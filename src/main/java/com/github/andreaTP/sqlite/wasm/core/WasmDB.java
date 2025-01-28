package com.github.andreaTP.sqlite.wasm.core;

import static com.github.andreaTP.sqlite.wasm.wasm.WasmDBExports.SQLITE_SERIALIZE_NOCOPY;
import static com.github.andreaTP.sqlite.wasm.wasm.WasmDBExports.SQLITE_UTF8;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import com.dylibso.chicory.wasm.types.ValueType;
import com.github.andreaTP.sqlite.wasm.BusyHandler;
import com.github.andreaTP.sqlite.wasm.Collation;
import com.github.andreaTP.sqlite.wasm.Function;
import com.github.andreaTP.sqlite.wasm.ProgressHandler;
import com.github.andreaTP.sqlite.wasm.SQLiteConfig;
import com.github.andreaTP.sqlite.wasm.SQLiteModule;
import com.github.andreaTP.sqlite.wasm.SQLiteUpdateListener;
import com.github.andreaTP.sqlite.wasm.wasm.BusyHandlerStore;
import com.github.andreaTP.sqlite.wasm.wasm.CollationStore;
import com.github.andreaTP.sqlite.wasm.wasm.ProgressHandlerStore;
import com.github.andreaTP.sqlite.wasm.wasm.UDFStore;
import com.github.andreaTP.sqlite.wasm.wasm.WasmDBExports;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

public class WasmDB extends DB {
    public static final int PTR_SIZE = 4;

    private static final WasmModule MODULE = SQLiteModule.load();

    private final Instance instance;
    private final WasiPreview1 wasiPreview1;
    private final WasmDBExports exports;

    // TODO: double-check proper cleanup of resources
    private final FileSystem fs;

    /** SQLite connection handle. */
    private int dbPtrPtr = 0;

    private int dbPtr = 0;

    private CollationStore collationStore = new CollationStore();

    private ImportValues imports() {
        return ImportValues.builder()
                .addFunction(wasiPreview1.toHostFunctions())
                .addFunction(
                        new HostFunction(
                                "env",
                                "xFunc",
                                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                                List.of(),
                                (inst, args) -> xFunc(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xStep",
                                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                                List.of(),
                                (inst, args) -> xStep(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xFinal",
                                List.of(ValueType.I32),
                                List.of(),
                                (inst, args) -> xFinal(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xValue",
                                List.of(ValueType.I32),
                                List.of(),
                                (inst, args) -> xValue(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xInverse",
                                List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                                List.of(),
                                (inst, args) -> xInverse(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xDestroy",
                                List.of(ValueType.I32),
                                List.of(),
                                (inst, args) -> xDestroy(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xProgress",
                                List.of(ValueType.I32),
                                List.of(ValueType.I32),
                                (inst, args) -> xProgress(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xBusy",
                                List.of(ValueType.I32, ValueType.I32),
                                List.of(ValueType.I32),
                                (inst, args) -> xBusy(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xCompare",
                                List.of(
                                        ValueType.I32,
                                        ValueType.I32,
                                        ValueType.I32,
                                        ValueType.I32,
                                        ValueType.I32),
                                List.of(ValueType.I32),
                                (inst, args) -> xCompare(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xDestroyCollation",
                                List.of(ValueType.I32),
                                List.of(),
                                (inst, args) -> xDestroyCollation(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xUpdate",
                                List.of(
                                        ValueType.I32,
                                        ValueType.I32,
                                        ValueType.I32,
                                        ValueType.I32,
                                        ValueType.I64),
                                List.of(),
                                (inst, args) -> xUpdate(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xCommit",
                                List.of(ValueType.I32),
                                List.of(ValueType.I32),
                                (inst, args) -> xCommit(args)))
                .addFunction(
                        new HostFunction(
                                "env",
                                "xRollback",
                                List.of(ValueType.I32),
                                List.of(),
                                (inst, args) -> xRollback(args)))
                .build();
    }

    public WasmDB(FileSystem fs, String url, String fileName, SQLiteConfig config)
            throws SQLException {
        super(url, fileName, config);
        this.fs = fs;

        // TODO: move this logic to another place
        // TODO: separate the concerns around the FileSystem when things are more stabilized
        Path target = fs.getPath("/");
        try {
            if (!java.nio.file.Files.exists(target)) {
                java.nio.file.Files.createDirectory(target);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory on the in-memory fs", e);
        }

        WasiOptions wasiOpts =
                WasiOptions.builder()
                        .inheritSystem()
                        .withDirectory(target.toString(), target)
                        .build();
        wasiPreview1 = WasiPreview1.builder().withOptions(wasiOpts).build();
        instance =
                Instance.builder(MODULE)
                        .withMachineFactory(SQLiteModule::create)
                        .withImportValues(imports())
                        // compile time option: -Wl,--initial-memory=327680
                        // means 5 pages initial memory
                        // increased with 3 more zeroes and now the test is passing
                        // in a decent time
                        // TODO: find as tradeoff between QueryTest.github720 and JDBCTest.hammer
                        .withMemoryLimits(new MemoryLimits(500))
                        .build();
        exports = new WasmDBExports(instance);
    }

    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    // https://www.sqlite.org/c3ref/progress_handler.html
    // only 1 progress handler at the time
    // TODO: do we need the Store at all?
    private long[] xProgress(long[] args) {
        int userData = (int) args[0];

        ProgressHandler f = ProgressHandlerStore.get(userData);

        try {
            int result = f.progress();
            return new long[] {result};
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }

    private long[] xBusy(long[] args) {
        int userData = (int) args[0];
        int nbPrevInvok = (int) args[1];

        BusyHandler f = BusyHandlerStore.get(userData);

        try {
            int result = f.callback(nbPrevInvok);
            return new long[] {result};
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }

    private long[] xDestroy(long[] args) {
        int funIdx = (int) args[0];

        UDFStore.free(funIdx);
        return null;
    }

    private long[] xFinal(long[] args) {
        int ctx = (int) args[0];

        int funIdx = exports.userData(ctx);
        Function f = UDFStore.get(funIdx);

        f.setContext(ctx);

        try {
            ((Function.Aggregate) f).xFinal();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }

    private long[] xValue(long[] args) {
        int ctx = (int) args[0];

        int funIdx = exports.userData(ctx);
        Function f = UDFStore.get(funIdx);

        f.setContext(ctx);

        try {
            ((Function.Window) f).xValue();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }

    private long[] xFunc(long[] args) {
        int ctx = (int) args[0];
        int argN = (int) args[1];
        int value = (int) args[2];

        int funIdx = exports.userData(ctx);
        Function f = UDFStore.get(funIdx);

        // TODO: verify if all of this is needed ...
        f.setContext(ctx);
        f.setValue(value);
        f.setArgs(argN);

        try {
            f.xFunc();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }

    private long[] xStep(long[] args) {
        int ctx = (int) args[0];
        int argN = (int) args[1];
        int value = (int) args[2];

        int funIdx = exports.userData(ctx);
        Function f = UDFStore.get(funIdx);

        // TODO: verify if all of this is needed ...
        f.setContext(ctx);
        f.setValue(value);
        f.setArgs(argN);

        try {
            ((Function.Aggregate) f).xStep();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }

    private long[] xInverse(long[] args) {
        int ctx = (int) args[0];
        int argN = (int) args[1];
        int value = (int) args[2];

        int funIdx = exports.userData(ctx);
        Function f = UDFStore.get(funIdx);

        // TODO: verify if all of this is needed ...
        f.setContext(ctx);
        f.setValue(value);
        f.setArgs(argN);

        try {
            ((Function.Window) f).xInverse();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }

    private long[] xCompare(long[] args) {
        int ctx = (int) args[0];
        int len1 = (int) args[1];
        int str1Ptr = (int) args[2];
        int len2 = (int) args[3];
        int str2Ptr = (int) args[4];

        Collation f = collationStore.get(ctx);

        String str1 =
                new String(instance.memory().readBytes(str1Ptr, len1), StandardCharsets.UTF_8);
        String str2 =
                new String(instance.memory().readBytes(str2Ptr, len2), StandardCharsets.UTF_8);

        return new long[] {f.xCompare(str1, str2)};
    }

    private long[] xDestroyCollation(long[] args) {
        int funIdx = (int) args[0];
        // no tmp data to be cleaned up
        // the collation will be freed with an explicit destroy call
        return null;
    }

    private static final int SQLITE_INSERT = 18;
    private static final int SQLITE_DELETE = 9;
    private static final int SQLITE_UPDATE = 23;

    private SQLiteUpdateListener.Type getUpdateType(int updateType) {
        switch (updateType) {
            case SQLITE_INSERT:
                return SQLiteUpdateListener.Type.INSERT;
            case SQLITE_DELETE:
                return SQLiteUpdateListener.Type.DELETE;
            case SQLITE_UPDATE:
                return SQLiteUpdateListener.Type.UPDATE;
            default:
                throw new IllegalArgumentException(
                        "Update type cannot be identified: " + updateType);
        }
    }

    private long[] xUpdate(long[] args) {
        int userData = (int) args[0]; // Unused
        SQLiteUpdateListener.Type type = getUpdateType((int) args[1]);
        int dbNamePtr = (int) args[2];
        int tablePtr = (int) args[3];
        long rowId = args[4];

        String dbName = instance.memory().readCString(dbNamePtr);
        String tableName = instance.memory().readCString(tablePtr);

        this.updateListeners.forEach(ul -> ul.onUpdate(type, dbName, tableName, rowId));

        // TODO: doublecheck if we do a double free
        exports.free(dbNamePtr);
        exports.free(tablePtr);

        // no tmp data to be cleaned up
        // the collation will be freed with an explicit destroy call
        return null;
    }

    private long[] xCommit(long[] args) {
        commitListeners.forEach(cl -> cl.onCommit());

        return new long[] {0};
    }

    private long[] xRollback(long[] args) {
        commitListeners.forEach(cl -> cl.onRollback());

        return null;
    }

    // safe access to the dbPointer
    private int dbPtr() throws SQLException {
        if (this.dbPtrPtr == 0 || this.dbPtr == 0) {
            throw new SQLException("Attempting to perform operations on a database not opened");
        }
        return this.dbPtr;
    }

    @Override
    protected synchronized void _open(String filename, int openFlags) throws SQLException {
        Path origin = Path.of(filename);
        Path dest = fs.getPath(filename);
        if (!filename.isEmpty() && Files.notExists(dest)) {
            // TODO: verify if works on windows
            if (Files.exists(origin)) {
                try (InputStream is = new FileInputStream(filename)) {
                    Files.createDirectories(dest);
                    java.nio.file.Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
                    var owner = Files.getOwner(origin);
                    Files.setOwner(dest, owner);
                    var permissions = Files.getPosixFilePermissions(origin);
                    Files.setPosixFilePermissions(dest, permissions);
                } catch (IOException e) {
                    SQLException msg =
                            DB.newSQLException(
                                    SQLITE_CANTOPEN,
                                    "Failed to map to memory the file: " + filename);
                    throw new SQLException(msg.getMessage(), e);
                }
            } else {
                try {
                    Files.createDirectories(dest);
                    // the "Files.notExists(dest)" should prevent this from happening
                    // still is observable in the tests
                    Files.deleteIfExists(dest);
                } catch (IOException e) {
                    throw new SQLException("Failed to create in memory the file: " + filename, e);
                }
            }
        }

        this.dbPtrPtr = exports.malloc(PTR_SIZE);
        int dbNamePtr = exports.allocCString(filename);

        int res = exports.openV2(dbNamePtr, dbPtrPtr, openFlags, 0);
        this.dbPtr = instance.memory().readInt(this.dbPtrPtr);
        if (res != SQLITE_OK) {
            int errCode = exports.extendedErrorcode(dbPtr());
            exports.close(dbPtr());
            throw DB.newSQLException(errCode, errmsg());
        }
        exports.free(dbNamePtr);
    }

    @Override
    protected SafeStmtPtr prepare(String sql) throws SQLException {
        int stmtPtrPtr = exports.malloc(PTR_SIZE);
        WasmDBExports.StringPtrSize str = exports.allocString(sql);

        int res = exports.prepareV2(dbPtr(), str.ptr(), str.size(), stmtPtrPtr, 0);
        exports.free(str.ptr());
        if (res != SQLITE_OK) {
            int errCode = exports.extendedErrorcode(dbPtr());
            // exports.close(dbPtr());
            throw DB.newSQLException(errCode, errmsg());
        }

        return new SafeStmtPtr(this, stmtPtrPtr);
    }

    @Override
    protected int finalize(long stmtPtrPtr) throws SQLException {
        int result = exports.finalize(exports.ptr((int) stmtPtrPtr));
        exports.free((int) stmtPtrPtr);
        return result;
    }

    @Override
    public int step(long stmtPtrPtr) throws SQLException {
        int result = exports.step(exports.ptr((int) stmtPtrPtr));
        if (result != SQLITE_OK) {
            return exports.extendedErrorcode(dbPtr());
        }
        return result;
    }

    @Override
    public int _exec(String sql) throws SQLException {
        int sqlBytesPtr = exports.allocCString(sql);

        int status = exports.exec(dbPtr(), sqlBytesPtr, 0, 0, 0);
        exports.free(sqlBytesPtr);
        if (status != SQLITE_OK) {
            int errCode = exports.extendedErrorcode(dbPtr());
            throw DB.newSQLException(errCode, errmsg());
        }

        return status;
    }

    @Override
    public long changes() throws SQLException {
        return exports.changes(dbPtr());
    }

    @Override
    public void interrupt() throws SQLException {
        throw new RuntimeException("interrupt not implemented in WasmDB");
    }

    @Override
    public void busy_timeout(int ms) throws SQLException {
        exports.busyTimeout(dbPtr(), ms);
    }

    @Override
    public void busy_handler(BusyHandler busyHandler) throws SQLException {
        int dbPtr = dbPtr();
        int busyHandlerPtr = BusyHandlerStore.registerBusyHandler(dbPtr, busyHandler);
        exports.busyHandler(dbPtr, busyHandlerPtr);
    }

    @Override
    String errmsg() throws SQLException {
        int errPtr = exports.errmsg(dbPtr());
        String err = instance.memory().readCString(errPtr);
        return err;
    }

    @Override
    public String libversion() throws SQLException {
        throw new RuntimeException("libversion not implemented in WasmDB");
    }

    @Override
    public long total_changes() throws SQLException {
        return exports.totalChanges(dbPtr());
    }

    @Override
    public int shared_cache(boolean enable) throws SQLException {
        throw new RuntimeException("shared_changes not implemented in WasmDB");
    }

    @Override
    public int enable_load_extension(boolean enable) throws SQLException {
        // TODO: extensions are not enabled in WASM/WASI
        return 0;
    }

    @Override
    protected void _close() throws SQLException {
        int dbPtr = dbPtr();
        ProgressHandlerStore.free(dbPtr);
        BusyHandlerStore.free(dbPtr);
        updateListeners.clear();
        commitListeners.clear();

        int res = exports.close(dbPtr);
        if (res != SQLITE_OK) {
            throw DB.newSQLException(res, errmsg());
        }

        exports.free(dbPtrPtr);

        // The handlers tests are failing when resetting those pointers
        // TODO: investigate the reason!
        this.dbPtr = 0;
        this.dbPtrPtr = 0;
        // TODO: when can we cleanup those resources?
        // Moving the library to load once this is a downside
        //        if (FS != null) {
        //            try {
        //                FS.close();
        //            } catch (IOException e) {
        //                throw new RuntimeException(e);
        //            }
        //        }
        //        if (WASI_PREVIEW_1 != null) {
        //            WASI_PREVIEW_1.close();
        //        }
    }

    @Override
    public int reset(long stmtPtrPtr) throws SQLException {
        return exports.reset(exports.ptr((int) stmtPtrPtr));
    }

    @Override
    public int clear_bindings(long stmtPtrPtr) throws SQLException {
        return exports.clearBindings(exports.ptr((int) stmtPtrPtr));
    }

    @Override
    int bind_parameter_count(long stmtPtrPtr) throws SQLException {
        return exports.bindParameterCount(exports.ptr((int) stmtPtrPtr));
    }

    @Override
    public int column_count(long stmtPtrPtr) throws SQLException {
        return exports.columnCount(exports.ptr((int) stmtPtrPtr));
    }

    @Override
    public int column_type(long stmtPtrPtr, int col) throws SQLException {
        return exports.columnType(exports.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public String column_decltype(long stmtPtrPtr, int col) throws SQLException {
        int ptr = exports.columnDeclType(exports.ptr((int) stmtPtrPtr), col);
        return instance.memory().readCString(ptr);
    }

    @Override
    public String column_table_name(long stmtPtrPtr, int col) throws SQLException {
        int ptr = exports.columnTableName(exports.ptr((int) stmtPtrPtr), col);
        if (ptr == 0) {
            return null;
        }
        return instance.memory().readCString(ptr);
    }

    @Override
    public String column_name(long stmtPtrPtr, int col) throws SQLException {
        int columnNamePtr = exports.columnName(exports.ptr((int) stmtPtrPtr), col);
        if (columnNamePtr == 0) {
            return null;
        }
        return instance.memory().readCString(columnNamePtr);
    }

    @Override
    public String column_text(long stmtPtrPtr, int col) throws SQLException {
        int stmtPtr = exports.ptr((int) stmtPtrPtr);
        int txtPtr = exports.columnText(stmtPtr, col);
        int txtLength = exports.columnBytes(stmtPtr, col);
        if (txtPtr == 0) {
            return null;
        }

        byte[] bytes = instance.memory().readBytes(txtPtr, txtLength);
        String result;
        //        // TODO: verify that the fallback should be here or not ...
        //        if (bytes.length > 0 && bytes[bytes.length - 1] == '\0') {
        //            byte[] resBytes = new byte[bytes.length - 1];
        //            System.arraycopy(bytes, 0, resBytes, 0, bytes.length - 1);
        //            result = new String(resBytes, StandardCharsets.UTF_8);
        //        } else {
        result = new String(bytes, StandardCharsets.UTF_8);
        //        }
        // TODO: verify if this result doesn't need a free ...
        // EXPORTS.free(txtPtr);
        return result;
    }

    @Override
    public byte[] column_blob(long stmtPtrPtr, int col) throws SQLException {
        return exports.columnBlob(exports.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public double column_double(long stmtPtrPtr, int col) throws SQLException {
        return exports.columnDouble(exports.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public long column_long(long stmtPtrPtr, int col) throws SQLException {
        return exports.columnLong(exports.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public int column_int(long stmtPtrPtr, int col) throws SQLException {
        return exports.columnInt(exports.ptr((int) stmtPtrPtr), col);
    }

    @Override
    int bind_null(long stmtPtrPtr, int pos) throws SQLException {
        return exports.bindNull(exports.ptr((int) stmtPtrPtr), pos);
    }

    @Override
    int bind_int(long stmtPtrPtr, int pos, int v) throws SQLException {
        return exports.bindInt(exports.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_long(long stmtPtrPtr, int pos, long v) throws SQLException {
        return exports.bindLong(exports.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_double(long stmtPtrPtr, int pos, double v) throws SQLException {
        return exports.bindDouble(exports.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_text(long stmtPtrPtr, int pos, String v) throws SQLException {
        WasmDBExports.StringPtrSize str = exports.allocString(v);
        int result = exports.bindText(exports.ptr((int) stmtPtrPtr), pos, str.ptr(), str.size());
        exports.free(str.ptr());
        return result;
    }

    @Override
    int bind_blob(long stmtPtrPtr, int pos, byte[] v) throws SQLException {
        int blobPtr = exports.malloc(v.length);
        instance.memory().write(blobPtr, v);
        int result = exports.bindBlob(exports.ptr((int) stmtPtrPtr), pos, blobPtr, v.length);
        exports.free(blobPtr);
        return result;
    }

    @Override
    public void result_null(long context) throws SQLException {
        throw new RuntimeException("result_null not implemented in WasmDB");
    }

    @Override
    public void result_text(long context, String val) throws SQLException {
        WasmDBExports.StringPtrSize txt = exports.allocString(val);
        exports.resultText((int) context, txt.ptr(), txt.size());
        exports.free(txt.ptr());
    }

    @Override
    public void result_blob(long context, byte[] v) throws SQLException {
        int blobPtr = exports.malloc(v.length);
        instance.memory().write(blobPtr, v);
        exports.resultBlob((int) context, blobPtr, v.length);
        exports.free(blobPtr);
    }

    @Override
    public void result_double(long context, double val) throws SQLException {
        exports.resultDouble((int) context, val);
    }

    @Override
    public void result_long(long context, long val) throws SQLException {
        exports.resultLong((int) context, val);
    }

    @Override
    public void result_int(long context, int val) throws SQLException {
        exports.resultInt((int) context, val);
    }

    @Override
    public void result_error(long context, String err) throws SQLException {
        throw new RuntimeException("result_error not implemented in WasmDB");
    }

    @Override
    public String value_text(Function f, int arg) throws SQLException {
        int valuePtrPtr = exports.ptr((int) f.getValueArg(arg));
        int txtPtr = exports.valueText(valuePtrPtr);
        String result = instance.memory().readCString(txtPtr);
        exports.free(txtPtr);
        return result;
    }

    @Override
    public byte[] value_blob(Function f, int arg) throws SQLException {
        int valuePtrPtr = exports.ptr((int) f.getValueArg(arg));
        int blobPtr = exports.valueBlob(valuePtrPtr);
        int length = exports.valueBytes(valuePtrPtr);
        byte[] blob = instance.memory().readBytes(blobPtr, length);
        exports.free(blobPtr);

        return blob;
    }

    @Override
    public double value_double(Function f, int arg) throws SQLException {
        int valuePtrPtr = exports.ptr((int) f.getValueArg(arg));
        return exports.valueDouble(valuePtrPtr);
    }

    @Override
    public long value_long(Function f, int arg) throws SQLException {
        int valuePtrPtr = exports.ptr((int) f.getValueArg(arg));
        return exports.valueLong(valuePtrPtr);
    }

    @Override
    public int value_int(Function f, int arg) throws SQLException {
        int valuePtrPtr = exports.ptr((int) f.getValueArg(arg));
        return exports.valueInt(valuePtrPtr);
    }

    @Override
    public int value_type(Function f, int arg) throws SQLException {
        throw new RuntimeException("value_type not implemented in WasmDB");
    }

    // TODO: seems like we need a synchronized from BusyHandlerTest.testMultiThreaded
    @Override
    public int create_function(String name, Function f, int nArgs, int flags) throws SQLException {
        int namePtr = exports.allocCString(name);
        int userData = UDFStore.registerFunction(f);

        int result;
        if (f instanceof Function.Aggregate) {
            boolean isWindow = f instanceof Function.Window;
            result =
                    exports.createFunctionAggregate(
                            dbPtr(), namePtr, nArgs, flags, userData, isWindow);
        } else {
            result = exports.createFunction(dbPtr(), namePtr, nArgs, flags, userData);
        }
        exports.free(namePtr);
        return result;
    }

    @Override
    public int destroy_function(String name) throws SQLException {
        int namePtr = exports.allocCString(name);
        int result = exports.createNullFunction(dbPtr(), namePtr);
        // TODO: implement free from UDFStore based on name
        exports.free(namePtr);
        return result;
    }

    @Override
    public int create_collation(String name, Collation c) throws SQLException {
        int namePtr = exports.allocCString(name);
        int userData = collationStore.registerCollation(name, c);

        int result = exports.createCollation(dbPtr(), namePtr, SQLITE_UTF8, userData);
        exports.free(namePtr);
        return result;
    }

    @Override
    public int destroy_collation(String name) throws SQLException {
        collationStore.free(name);

        int namePtr = exports.allocCString(name);

        int result = exports.destroyCollation(dbPtr(), namePtr);
        exports.free(namePtr);
        return result;
    }

    @Override
    public int backup(String dbName, String destFileName, ProgressObserver observer)
            throws SQLException {
        throw new RuntimeException("backup not implemented in WasmDB");
    }

    @Override
    public int backup(
            String dbName,
            String destFileName,
            ProgressObserver observer,
            int sleepTimeMillis,
            int nTimeouts,
            int pagesPerStep)
            throws SQLException {
        throw new RuntimeException("backup not implemented in WasmDB");
    }

    @Override
    public int restore(String dbName, String sourceFileName, ProgressObserver observer)
            throws SQLException {
        throw new RuntimeException("restore not implemented in WasmDB");
    }

    @Override
    public int restore(
            String dbName,
            String sourceFileName,
            ProgressObserver observer,
            int sleepTimeMillis,
            int nTimeouts,
            int pagesPerStep)
            throws SQLException {
        throw new RuntimeException("restore not implemented in WasmDB");
    }

    @Override
    public int limit(int id, int value) throws SQLException {
        return exports.limit(dbPtr(), id, value);
    }

    @Override
    public void register_progress_handler(int vmCalls, ProgressHandler progressHandler)
            throws SQLException {
        int progressHandlerIdx = dbPtr();
        ProgressHandlerStore.registerProgressHandler(progressHandlerIdx, progressHandler);
        exports.progressHandler(dbPtr(), vmCalls, progressHandlerIdx);
    }

    @Override
    public void clear_progress_handler() throws SQLException {
        ProgressHandlerStore.free(dbPtr());
        exports.progressHandler(dbPtr(), 0, 0);
    }

    @Override
    boolean[][] column_metadata(long stmtPtrPtr) throws SQLException {
        int stmtPtr = exports.ptr((int) stmtPtrPtr);
        int colCount = exports.columnCount(stmtPtr);

        boolean[][] result = new boolean[colCount][3];

        for (int i = 0; i < colCount; i++) {
            // load passed column name and table name
            int zColumnNamePtr = exports.columnName(stmtPtr, i);
            int zTableNamePtr = exports.columnTableName(stmtPtr, i);

            int pNotNullPtr = exports.malloc(1);
            int pPrimaryKeyPtr = exports.malloc(1);
            int pAutoincPtr = exports.malloc(1);

            instance.memory().writeByte(pNotNullPtr, (byte) 0);
            instance.memory().writeByte(pPrimaryKeyPtr, (byte) 0);
            instance.memory().writeByte(pAutoincPtr, (byte) 0);

            int res =
                    exports.columnMetadata(
                            dbPtr(),
                            zTableNamePtr,
                            zColumnNamePtr,
                            pNotNullPtr,
                            pPrimaryKeyPtr,
                            pAutoincPtr);
            assert (res == SQLITE_OK);

            result[i][0] = instance.memory().read(pNotNullPtr) > 0;
            result[i][1] = instance.memory().read(pPrimaryKeyPtr) > 0;
            result[i][2] = instance.memory().read(pAutoincPtr) > 0;
            exports.free(pNotNullPtr);
            exports.free(pPrimaryKeyPtr);
            exports.free(pAutoincPtr);
        }
        return result;
    }

    @Override
    void set_commit_listener(boolean enabled) {
        if (enabled) {
            exports.commitHook(this.dbPtr, 0);
            exports.rollbackHook(this.dbPtr, 0);
        } else {
            exports.deleteCommitHook(this.dbPtr);
            exports.deleteRollbackHook(this.dbPtr);
        }
    }

    @Override
    void set_update_listener(boolean enabled) {
        if (enabled) {
            exports.updateHook(this.dbPtr, 0);
        } else {
            exports.deleteUpdateHook(this.dbPtr);
        }
    }

    @Override
    public byte[] serialize(String schema) throws SQLException {
        int schemaPtr = exports.allocCString(schema);
        int sizePtr = exports.malloc(8);

        int buffPtr = exports.serialize(dbPtr(), schemaPtr, sizePtr, SQLITE_SERIALIZE_NOCOPY);
        if (buffPtr == 0) {
            // This happens if we start without a deserialized database
            buffPtr = exports.serialize(dbPtr(), schemaPtr, sizePtr, 0);
        }

        long buffSize = instance.memory().readLong(sizePtr);
        exports.free(sizePtr);
        exports.free(schemaPtr);

        byte[] result = instance.memory().readBytes(buffPtr, (int) buffSize);
        exports.free(buffPtr);

        return result;
    }

    @Override
    public void deserialize(String schema, byte[] buff) throws SQLException {
        int schemaPtr = exports.allocCString(schema);
        int buffPtr = exports.malloc(buff.length);
        instance.memory().write(buffPtr, buff);

        int res = exports.deserialize(dbPtr(), schemaPtr, buffPtr, buff.length);
        if (res != SQLITE_OK) {
            throw DB.newSQLException(res, errmsg());
        }

        // exports.free(schemaPtr);
    }

    /**
     * Getter for native pointer to validate memory is properly cleaned up in unit tests
     *
     * @return a native pointer to validate memory is properly cleaned up in unit tests
     */
    long getProgressHandler() throws SQLException {
        if (dbPtr == 0 || dbPtrPtr == 0) {
            return 0L;
        }

        if (ProgressHandlerStore.isEmpty(dbPtr())) {
            return 0L;
        } else {
            return 1L;
        }
    }

    /**
     * Getter for native pointer to validate memory is properly cleaned up in unit tests
     *
     * @return a native pointer to validate memory is properly cleaned up in unit tests
     */
    long getBusyHandler() throws SQLException {
        if (dbPtr == 0 || dbPtrPtr == 0) {
            return 0L;
        }

        if (BusyHandlerStore.isEmpty(dbPtr())) {
            return 0L;
        } else {
            return 1L;
        }
    }

    long getUpdateListener() {
        if (dbPtr == 0 || dbPtrPtr == 0) {
            return 0L;
        }

        if (updateListeners.isEmpty()) {
            return 0L;
        } else {
            return 1L;
        }
    }

    // This call is expensive as it will start an extra module
    // TODO: check if we can use caches
    // TODO: refactor instance creation!
    public static String version() {
        WasiOptions wasiOpts = WasiOptions.builder().build();

        try (WasiPreview1 wasiPreview1 = WasiPreview1.builder().withOptions(wasiOpts).build()) {
            Instance tmp =
                    Instance.builder(MODULE)
                            .withMachineFactory(SQLiteModule::create)
                            .withImportValues(
                                    ImportValues.builder()
                                            .addFunction(wasiPreview1.toHostFunctions())
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xFunc",
                                                            List.of(
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I32),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xStep",
                                                            List.of(
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I32),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xFinal",
                                                            List.of(ValueType.I32),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xValue",
                                                            List.of(ValueType.I32),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xInverse",
                                                            List.of(
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I32),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xDestroy",
                                                            List.of(ValueType.I32),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xProgress",
                                                            List.of(ValueType.I32),
                                                            List.of(ValueType.I32),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xBusy",
                                                            List.of(ValueType.I32, ValueType.I32),
                                                            List.of(ValueType.I32),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xCompare",
                                                            List.of(
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I32),
                                                            List.of(ValueType.I32),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xDestroyCollation",
                                                            List.of(ValueType.I32),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xUpdate",
                                                            List.of(
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I32,
                                                                    ValueType.I64),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xCommit",
                                                            List.of(ValueType.I32),
                                                            List.of(ValueType.I32),
                                                            (inst, args) -> null))
                                            .addFunction(
                                                    new HostFunction(
                                                            "env",
                                                            "xRollback",
                                                            List.of(ValueType.I32),
                                                            List.of(),
                                                            (inst, args) -> null))
                                            .build())
                            .withStart(false)
                            .build();
            int ptr = new WasmDBExports(tmp).version();

            String version = tmp.memory().readCString(ptr);
            return version;
        }
    }
}
