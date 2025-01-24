package com.github.andreaTP.sqlite.wasm.core;

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

    public String version() {
        int ptr = exports.version();
        String version = instance.memory().readCString(ptr);
        return version;
    }

    /** SQLite connection handle. */
    private int dbPtrPtr = 0;

    private int dbPtr = 0;

    public WasmDB(FileSystem fs, String url, String fileName, SQLiteConfig config)
            throws SQLException {
        super(url, fileName, config);
        this.fs = fs;
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
                                                        (inst, args) -> xFunc(args)))
                                        .addFunction(
                                                new HostFunction(
                                                        "env",
                                                        "xStep",
                                                        List.of(
                                                                ValueType.I32,
                                                                ValueType.I32,
                                                                ValueType.I32),
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
                                                        List.of(
                                                                ValueType.I32,
                                                                ValueType.I32,
                                                                ValueType.I32),
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
                                        .build())
                        .withMemoryLimits(new MemoryLimits(10, MemoryLimits.MAX_PAGES))
                        .build();
        exports = new WasmDBExports(instance);
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
            // TODO: decide how to handle the checked exception thrown here
        } catch (SQLException e) {
            throw new RuntimeException("wrapped SQLException", e);
        }
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
            // TODO: decide how to handle the checked exception thrown here
        } catch (SQLException e) {
            throw new RuntimeException("wrapped SQLException", e);
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
            // TODO: decide how to handle the checked exception thrown here
        } catch (SQLException e) {
            throw new RuntimeException("wrapped SQLException", e);
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
            // TODO: decide how to handle the checked exception thrown here
        } catch (SQLException e) {
            throw new RuntimeException("wrapped SQLException", e);
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
            // TODO: decide how to handle the checked exception thrown here
        } catch (SQLException e) {
            throw new RuntimeException("wrapped SQLException", e);
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
            // TODO: decide how to handle the checked exception thrown here
        } catch (SQLException e) {
            throw new RuntimeException("wrapped SQLException", e);
        }
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
        Path dest = fs.getPath(filename);
        if (!filename.isEmpty() && Files.notExists(dest)) {
            // TODO: verify if works on windows
            if (Files.exists(Path.of(filename))) {
                try (InputStream is = new FileInputStream(filename)) {
                    Files.createDirectories(dest);
                    java.nio.file.Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new SQLException("Failed to map to memory the file: " + filename, e);
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
        int dbNamePtr = exports.allocCString(filename).ptr();

        int res = exports.openV2(dbNamePtr, dbPtrPtr, openFlags, 0);
        this.dbPtr = instance.memory().readInt(this.dbPtrPtr);
        if (res != SQLITE_OK) {
            int errCode = exports.extendedErrorcode(dbPtr);
            String errmsg = errmsg();
            exports.close(dbPtr);
            throw new SQLException(
                    "Failed to open database "
                            + filename
                            + ", error code: "
                            + errCode
                            + ", error message: "
                            + errmsg);
        }
        exports.free(dbNamePtr);
    }

    @Override
    protected SafeStmtPtr prepare(String sql) throws SQLException {
        int stmtPtrPtr = exports.malloc(PTR_SIZE);
        WasmDBExports.StringPtrSize str = exports.allocCString(sql);

        exports.prepareV2(dbPtr(), str.ptr(), str.size(), stmtPtrPtr, 0);
        exports.free(str.ptr());

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
        return exports.step(exports.ptr((int) stmtPtrPtr));
    }

    @Override
    public int _exec(String sql) throws SQLException {
        int sqlBytesPtr = exports.allocCString(sql).ptr();

        int status = exports.exec(dbPtr(), sqlBytesPtr, 0, 0, 0);
        exports.free(sqlBytesPtr);
        if (status != SQLITE_OK) {
            String errmsg = errmsg();
            throw new SQLException(
                    "Failed to exec "
                            + sql
                            + ", returned error code: "
                            + status
                            + ", error message: "
                            + errmsg);
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
        throw new RuntimeException("busy_handler not implemented in WasmDB");
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
        // throw new RuntimeException("enable_load_extension not implemented in WasmDB");
        // TODO: implement me skip for now
        return 0;
    }

    @Override
    protected void _close() throws SQLException {
        ProgressHandlerStore.free(progressHandlerIdx);

        exports.close(dbPtr());
        exports.free(dbPtrPtr);
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
    public String column_decltype(long stmt, int col) throws SQLException {
        throw new RuntimeException("column_decltype not implemented in WasmDB");
    }

    @Override
    public String column_table_name(long stmt, int col) throws SQLException {
        throw new RuntimeException("column_table_name not implemented in WasmDB");
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
        // TODO: verify that the fallback should be here or not ...
        if (bytes.length > 0 && bytes[bytes.length - 1] == '\0') {
            byte[] resBytes = new byte[bytes.length - 1];
            System.arraycopy(bytes, 0, resBytes, 0, bytes.length - 1);
            result = new String(resBytes, StandardCharsets.UTF_8);
        } else {
            result = new String(bytes, StandardCharsets.UTF_8);
        }
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
        WasmDBExports.StringPtrSize str = exports.allocCString(v);
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
        WasmDBExports.StringPtrSize txt = exports.allocCString(val);
        exports.resultText((int) context, txt.ptr(), txt.size());
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
        // TODO: count the bytes and do this more acurately
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

    @Override
    public int create_function(String name, Function f, int nArgs, int flags) throws SQLException {
        WasmDBExports.StringPtrSize namePtrSize = exports.allocCString(name);
        int userData = UDFStore.registerFunction(f);

        if (f instanceof Function.Aggregate) {
            boolean isWindow = f instanceof Function.Window;
            return exports.createFunctionAggregate(
                    dbPtr(), namePtrSize.ptr(), nArgs, flags, userData, isWindow);
        } else {
            return exports.createFunction(dbPtr(), namePtrSize.ptr(), nArgs, flags, userData);
        }
    }

    @Override
    public int destroy_function(String name) throws SQLException {
        WasmDBExports.StringPtrSize namePtrSize = exports.allocCString(name);
        int result = exports.createNullFunction(dbPtr(), namePtrSize.ptr());
        exports.free(namePtrSize.ptr());
        return result;
    }

    @Override
    public int create_collation(String name, Collation c) throws SQLException {
        throw new RuntimeException("create_collation not implemented in WasmDB");
    }

    @Override
    public int destroy_collation(String name) throws SQLException {
        throw new RuntimeException("destroy_collation not implemented in WasmDB");
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

    // only one per connection, verify!
    private int progressHandlerIdx = 0;

    @Override
    public void register_progress_handler(int vmCalls, ProgressHandler progressHandler)
            throws SQLException {
        progressHandlerIdx = ProgressHandlerStore.registerProgressHandler(progressHandler);
        exports.progressHandler(dbPtr(), vmCalls, progressHandlerIdx);
    }

    @Override
    public void clear_progress_handler() throws SQLException {
        ProgressHandlerStore.free(progressHandlerIdx);
        progressHandlerIdx = 0;
        exports.progressHandler(dbPtr(), 0, 0);
    }

    @Override
    boolean[][] column_metadata(long stmt) throws SQLException {
        throw new RuntimeException("column_metadata not implemented in WasmDB");
    }

    @Override
    void set_commit_listener(boolean enabled) {
        throw new RuntimeException("set_commit_listener not implemented in WasmDB");
    }

    @Override
    void set_update_listener(boolean enabled) {
        throw new RuntimeException("set_update_listener not implemented in WasmDB");
    }

    @Override
    public byte[] serialize(String schema) throws SQLException {
        throw new RuntimeException("serialize not implemented in WasmDB");
    }

    @Override
    public void deserialize(String schema, byte[] buff) throws SQLException {
        throw new RuntimeException("deserialize not implemented in WasmDB");
    }

    /**
     * Getter for native pointer to validate memory is properly cleaned up in unit tests
     *
     * @return a native pointer to validate memory is properly cleaned up in unit tests
     */
    long getProgressHandler() {
        if (ProgressHandlerStore.get(progressHandlerIdx) != null) {
            return 1L;
        } else {
            return 0L;
        }
    }
}
