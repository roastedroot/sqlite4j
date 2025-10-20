package io.roastedroot.sqlite4j.core;

import static io.roastedroot.sqlite4j.core.wasm.WasmDBExports.SQLITE_SERIALIZE_NOCOPY;
import static io.roastedroot.sqlite4j.core.wasm.WasmDBExports.SQLITE_UTF8;

import com.dylibso.chicory.runtime.ByteArrayMemory;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import io.roastedroot.sqlite4j.BusyHandler;
import io.roastedroot.sqlite4j.Collation;
import io.roastedroot.sqlite4j.Function;
import io.roastedroot.sqlite4j.ProgressHandler;
import io.roastedroot.sqlite4j.SQLiteConfig;
import io.roastedroot.sqlite4j.SQLiteErrorCode;
import io.roastedroot.sqlite4j.SQLiteException;
import io.roastedroot.sqlite4j.SQLiteModule;
import io.roastedroot.sqlite4j.SQLiteUpdateListener;
import io.roastedroot.sqlite4j.Version;
import io.roastedroot.sqlite4j.core.wasm.BusyHandlerStore;
import io.roastedroot.sqlite4j.core.wasm.CollationStore;
import io.roastedroot.sqlite4j.core.wasm.DummyWasmDBImports;
import io.roastedroot.sqlite4j.core.wasm.ProgressHandlerStore;
import io.roastedroot.sqlite4j.core.wasm.UDFStore;
import io.roastedroot.sqlite4j.core.wasm.WasmDBExports;
import io.roastedroot.sqlite4j.core.wasm.WasmDBImports;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class WasmDB extends DB implements WasmDBImports {
    public static final int PTR_SIZE = 4;
    private static final WasmModule MODULE = SQLiteModule.load();

    private final Instance instance;
    private final WasiPreview1 wasiPreview1;
    private final WasmDBExports lib;
    private final FileSystem fs;
    private final boolean isMemory;

    /** SQLite connection handle. */
    private int dbPtrPtr = 0;

    private int dbPtr = 0;

    // Collations are dedicated per connection
    private CollationStore collationStore = new CollationStore();

    public WasmDB(FileSystem fs, String url, String fileName, SQLiteConfig config, boolean isMemory)
            throws SQLException {
        super(url, fileName, config);
        this.fs = fs;
        this.isMemory = isMemory;

        Path target = fs.getPath("/");
        WasiOptions wasiOpts =
                WasiOptions.builder()
                        .inheritSystem()
                        .withDirectory(target.toString(), target)
                        .build();

        wasiPreview1 = WasiPreview1.builder().withOptions(wasiOpts).build();
        instance =
                Instance.builder(MODULE)
                        .withMachineFactory(SQLiteModule::create)
                        .withMemoryFactory(ByteArrayMemory::new)
                        .withImportValues(
                                ImportValues.builder()
                                        .addFunction(wasiPreview1.toHostFunctions())
                                        .addFunction(toHostFunctions())
                                        .build())
                        // compile time option: -Wl,--initial-memory=327680
                        // means 5 pages initial memory
                        // increased with 3 more zeroes and now the test is passing
                        // in a decent time
                        // TODO: find as tradeoff between QueryTest.github720 and JDBCTest.hammer
                        .withMemoryLimits(new MemoryLimits(500, Memory.RUNTIME_MAX_PAGES))
                        .build();
        lib = new WasmDBExports(instance);
    }

    // TODO: find a better way for doing this
    // throw a wrapper exception and unwrap it in the "safeRun" of the statement, maybe in the
    // future
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    // https://www.sqlite.org/c3ref/progress_handler.html
    // only 1 progress handler at the time
    @Override
    public int xProgress(int userData) {
        ProgressHandler f = ProgressHandlerStore.get(userData);

        try {
            int result = f.progress();
            return result;
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return SQLITE_ERROR;
    }

    @Override
    public int xBusy(int userData, int nbPrevInvok) {
        BusyHandler f = BusyHandlerStore.get(userData);

        try {
            int result = f.callback(nbPrevInvok);
            return result;
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return SQLITE_ERROR;
    }

    @Override
    public void xDestroy(int funIdx) {
        UDFStore.free(funIdx);
    }

    @Override
    public void xFinal(int ctx) {
        int funIdx = lib.userData(ctx);
        Function f = UDFStore.get(funIdx);

        f.setContext(ctx);

        try {
            ((Function.Aggregate) f).xFinal();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    @Override
    public void xValue(int ctx) {
        int funIdx = lib.userData(ctx);
        Function f = UDFStore.get(funIdx);

        f.setContext(ctx);

        try {
            ((Function.Window) f).xValue();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    @Override
    public void xFunc(int ctx, int argN, int value) {
        int funIdx = lib.userData(ctx);
        Function f = UDFStore.get(funIdx);

        f.setContext(ctx);
        f.setValue(value);
        f.setArgs(argN);

        try {
            f.xFunc();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    @Override
    public void xStep(int ctx, int argN, int value) {
        int funIdx = lib.userData(ctx);
        Function f = UDFStore.get(funIdx);

        f.setContext(ctx);
        f.setValue(value);
        f.setArgs(argN);

        try {
            ((Function.Aggregate) f).xStep();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    @Override
    public void xInverse(int ctx, int argN, int value) {
        int funIdx = lib.userData(ctx);
        Function f = UDFStore.get(funIdx);

        f.setContext(ctx);
        f.setValue(value);
        f.setArgs(argN);

        try {
            ((Function.Window) f).xInverse();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    @Override
    public int xCompare(int ctx, int len1, int str1Ptr, int len2, int str2Ptr) {
        Collation f = collationStore.get(ctx);

        String str1 =
                new String(instance.memory().readBytes(str1Ptr, len1), StandardCharsets.UTF_8);
        String str2 =
                new String(instance.memory().readBytes(str2Ptr, len2), StandardCharsets.UTF_8);

        return f.xCompare(str1, str2);
    }

    @Override
    public void xDestroyCollation(int funIdx) {
        // no tmp data to be cleaned up
        // the collation will be freed with an explicit destroy call
    }

    private static final int SQLITE_INSERT = 18;
    private static final int SQLITE_DELETE = 9;
    private static final int SQLITE_UPDATE = 23;

    private static SQLiteUpdateListener.Type getUpdateType(int updateType) {
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

    @Override
    public void xUpdate(int userData, int tpe, int dbNamePtr, int tablePtr, long rowId) {
        SQLiteUpdateListener.Type type = getUpdateType(tpe);

        String dbName = instance.memory().readCString(dbNamePtr);
        String tableName = instance.memory().readCString(tablePtr);

        this.updateListeners.forEach(ul -> ul.onUpdate(type, dbName, tableName, rowId));

        // TODO: doublecheck if we do a double free
        lib.free(dbNamePtr);
        lib.free(tablePtr);

        // no tmp data to be cleaned up
        // the collation will be freed with an explicit destroy call
    }

    @Override
    public int xCommit(int userData) {
        commitListeners.forEach(cl -> cl.onCommit());
        return 0;
    }

    @Override
    public void xRollback(int userData) {
        commitListeners.forEach(cl -> cl.onRollback());
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
        if (!isMemory) {
            Path origin = Path.of(filename);
            Path dest = fs.getPath(filename);
            if (!filename.isEmpty() && Files.notExists(dest)) {
                if (!Files.exists(origin) && (openFlags & SQLITE_OPEN_CREATE) != 0) {
                    try {
                        if (origin.getParent() != null) {
                            Files.createDirectories(origin.getParent());
                        }
                        Files.createFile(origin);
                    } catch (IOException e) {
                        SQLException msg =
                                DB.newSQLException(
                                        SQLITE_CANTOPEN, "Failed to create db file: " + filename);
                        throw new SQLException(msg.getMessage(), e);
                    }
                }
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
                    SQLException msg =
                            DB.newSQLException(
                                    SQLITE_CANTOPEN, "Database file doesn't exists: " + filename);
                    throw new SQLException(msg.getMessage());
                }
            }
        }

        this.dbPtrPtr = lib.malloc(PTR_SIZE);
        int dbNamePtr = lib.allocCString(filename);

        int res = lib.openV2(dbNamePtr, dbPtrPtr, openFlags, 0);
        this.dbPtr = instance.memory().readInt(this.dbPtrPtr);
        if (res != SQLITE_OK) {
            int errCode = lib.extendedErrorcode(dbPtr());
            lib.close(dbPtr());
            throw DB.newSQLException(errCode, errmsg());
        }
        // exports.free(dbNamePtr);
    }

    @Override
    protected SafeStmtPtr prepare(String sql) throws SQLException {
        int stmtPtrPtr = lib.malloc(PTR_SIZE);
        WasmDBExports.StringPtrSize str = lib.allocString(sql);

        int res = lib.prepareV2(dbPtr(), str.ptr(), str.size(), stmtPtrPtr, 0);
        lib.free(str.ptr());
        if (res != SQLITE_OK) {
            int errCode = lib.extendedErrorcode(dbPtr());
            // exports.close(dbPtr());
            throw DB.newSQLException(errCode, errmsg());
        }

        return new SafeStmtPtr(this, stmtPtrPtr);
    }

    @Override
    protected int finalize(long stmtPtrPtr) throws SQLException {
        int result = lib.finalize(lib.ptr((int) stmtPtrPtr));
        return result;
    }

    @Override
    public int step(long stmtPtrPtr) throws SQLException {
        int result = lib.step(lib.ptr((int) stmtPtrPtr));
        if (result != SQLITE_OK) {
            return lib.extendedErrorcode(dbPtr());
        }
        return result;
    }

    @Override
    public int _exec(String sql) throws SQLException {
        int sqlBytesPtr = lib.allocCString(sql);

        int status = lib.exec(dbPtr(), sqlBytesPtr, 0, 0, 0);
        lib.free(sqlBytesPtr);
        if (status != SQLITE_OK) {
            int errCode = lib.extendedErrorcode(dbPtr());
            throw DB.newSQLException(errCode, errmsg());
        }

        return status;
    }

    @Override
    public long changes() throws SQLException {
        return lib.changes(dbPtr());
    }

    @Override
    public void interrupt() throws SQLException {
        lib.interrupt(dbPtr());
    }

    @Override
    public void busy_timeout(int ms) throws SQLException {
        lib.busyTimeout(dbPtr(), ms);
    }

    @Override
    public void busy_handler(BusyHandler busyHandler) throws SQLException {
        int dbPtr = dbPtr();
        int busyHandlerPtr = BusyHandlerStore.registerBusyHandler(dbPtr, busyHandler);
        lib.busyHandler(dbPtr, busyHandlerPtr);
    }

    @Override
    String errmsg() throws SQLException {
        int errPtr = lib.errmsg(dbPtr());
        String err = instance.memory().readCString(errPtr);
        return err;
    }

    @Override
    public String libversion() throws SQLException {
        return Version.libVersion();
    }

    @Override
    public long total_changes() throws SQLException {
        return lib.totalChanges(dbPtr());
    }

    @Override
    public int shared_cache(boolean enable) throws SQLException {
        if (enable) {
            throw new SQLException("Shared cache is disabled in the WASM build");
        }
        return 0;
    }

    @Override
    public int enable_load_extension(boolean enable) throws SQLException {
        if (enable) {
            throw new RuntimeException("load extension cannot be enabled in WasmDB");
        }
        return 0;
    }

    @Override
    protected void _close() throws SQLException {
        int dbPtr = dbPtr();
        if (dbPtr != 0) {
            ProgressHandlerStore.free(dbPtr);
            BusyHandlerStore.free(dbPtr);
            updateListeners.clear();
            commitListeners.clear();

            int res = lib.close(dbPtr);
            if (res != SQLITE_OK) {
                throw DB.newSQLException(res, errmsg());
            }

            lib.free(dbPtrPtr);

            // The handlers tests are failing when resetting those pointers
            // TODO: investigate the reason!
            this.dbPtr = 0;
            this.dbPtrPtr = 0;
        }
        if (wasiPreview1 != null) {
            wasiPreview1.close();
        }
    }

    @Override
    public int reset(long stmtPtrPtr) throws SQLException {
        return lib.reset(lib.ptr((int) stmtPtrPtr));
    }

    @Override
    public int clear_bindings(long stmtPtrPtr) throws SQLException {
        return lib.clearBindings(lib.ptr((int) stmtPtrPtr));
    }

    @Override
    int bind_parameter_count(long stmtPtrPtr) throws SQLException {
        return lib.bindParameterCount(lib.ptr((int) stmtPtrPtr));
    }

    @Override
    public int column_count(long stmtPtrPtr) throws SQLException {
        return lib.columnCount(lib.ptr((int) stmtPtrPtr));
    }

    @Override
    public int column_type(long stmtPtrPtr, int col) throws SQLException {
        return lib.columnType(lib.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public String column_decltype(long stmtPtrPtr, int col) throws SQLException {
        int ptr = lib.columnDeclType(lib.ptr((int) stmtPtrPtr), col);
        if (ptr == 0) {
            return null;
        } else {
            return instance.memory().readCString(ptr);
        }
    }

    @Override
    public String column_table_name(long stmtPtrPtr, int col) throws SQLException {
        int ptr = lib.columnTableName(lib.ptr((int) stmtPtrPtr), col);
        if (ptr == 0) {
            return null;
        }
        return instance.memory().readCString(ptr);
    }

    @Override
    public String column_name(long stmtPtrPtr, int col) throws SQLException {
        int columnNamePtr = lib.columnName(lib.ptr((int) stmtPtrPtr), col);
        if (columnNamePtr == 0) {
            return null;
        }
        return instance.memory().readCString(columnNamePtr);
    }

    @Override
    public String column_text(long stmtPtrPtr, int col) throws SQLException {
        int stmtPtr = lib.ptr((int) stmtPtrPtr);
        int txtPtr = lib.columnText(stmtPtr, col);
        int txtLength = lib.columnBytes(stmtPtr, col);
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
        // TODO: verify if this result doesn't need a free, looks like no
        // exports.free(txtPtr);
        return result;
    }

    @Override
    public byte[] column_blob(long stmtPtrPtr, int col) throws SQLException {
        return lib.columnBlob(lib.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public double column_double(long stmtPtrPtr, int col) throws SQLException {
        return lib.columnDouble(lib.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public long column_long(long stmtPtrPtr, int col) throws SQLException {
        return lib.columnLong(lib.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public int column_int(long stmtPtrPtr, int col) throws SQLException {
        return lib.columnInt(lib.ptr((int) stmtPtrPtr), col);
    }

    @Override
    int bind_null(long stmtPtrPtr, int pos) throws SQLException {
        return lib.bindNull(lib.ptr((int) stmtPtrPtr), pos);
    }

    @Override
    int bind_int(long stmtPtrPtr, int pos, int v) throws SQLException {
        return lib.bindInt(lib.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_long(long stmtPtrPtr, int pos, long v) throws SQLException {
        return lib.bindLong(lib.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_double(long stmtPtrPtr, int pos, double v) throws SQLException {
        return lib.bindDouble(lib.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_text(long stmtPtrPtr, int pos, String v) throws SQLException {
        WasmDBExports.StringPtrSize str = lib.allocString(v);
        int result = lib.bindText(lib.ptr((int) stmtPtrPtr), pos, str.ptr(), str.size());
        lib.free(str.ptr());
        return result;
    }

    @Override
    int bind_blob(long stmtPtrPtr, int pos, byte[] v) throws SQLException {
        int blobPtr = lib.malloc(v.length);
        instance.memory().write(blobPtr, v);
        int result = lib.bindBlob(lib.ptr((int) stmtPtrPtr), pos, blobPtr, v.length);
        lib.free(blobPtr);
        return result;
    }

    @Override
    public void result_null(long context) throws SQLException {
        lib.resultNull((int) context);
    }

    @Override
    public void result_text(long context, String val) throws SQLException {
        if (val == null) {
            result_null(context);
            return;
        }

        WasmDBExports.StringPtrSize txt = lib.allocString(val);
        lib.resultText((int) context, txt.ptr(), txt.size());
        lib.free(txt.ptr());
    }

    @Override
    public void result_blob(long context, byte[] v) throws SQLException {
        int blobPtr = lib.malloc(v.length);
        instance.memory().write(blobPtr, v);
        lib.resultBlob((int) context, blobPtr, v.length);
        lib.free(blobPtr);
    }

    @Override
    public void result_double(long context, double val) throws SQLException {
        lib.resultDouble((int) context, val);
    }

    @Override
    public void result_long(long context, long val) throws SQLException {
        lib.resultLong((int) context, val);
    }

    @Override
    public void result_int(long context, int val) throws SQLException {
        lib.resultInt((int) context, val);
    }

    @Override
    public void result_error(long context, String err) throws SQLException {
        if (err == null || err.isEmpty()) {
            lib.resultErrorNomem((int) context);
            return;
        }

        byte[] v = err.getBytes(StandardCharsets.UTF_8);
        int blobPtr = lib.malloc(v.length);
        instance.memory().write(blobPtr, v);
        lib.resultError((int) context, blobPtr, v.length);
        lib.free(blobPtr);
    }

    @Override
    public String value_text(Function f, int arg) throws SQLException {
        int valuePtrPtr = lib.ptr((int) f.getValueArg(arg));
        int txtPtr = lib.valueText(valuePtrPtr);
        String result = instance.memory().readCString(txtPtr);
        lib.free(txtPtr);
        return result;
    }

    @Override
    public byte[] value_blob(Function f, int arg) throws SQLException {
        int valuePtrPtr = lib.ptr((int) f.getValueArg(arg));
        int blobPtr = lib.valueBlob(valuePtrPtr);
        int length = lib.valueBytes(valuePtrPtr);
        byte[] blob = instance.memory().readBytes(blobPtr, length);
        lib.free(blobPtr);
        return blob;
    }

    @Override
    public double value_double(Function f, int arg) throws SQLException {
        int valuePtrPtr = lib.ptr((int) f.getValueArg(arg));
        return lib.valueDouble(valuePtrPtr);
    }

    @Override
    public long value_long(Function f, int arg) throws SQLException {
        int valuePtrPtr = lib.ptr((int) f.getValueArg(arg));
        return lib.valueLong(valuePtrPtr);
    }

    @Override
    public int value_int(Function f, int arg) throws SQLException {
        int valuePtrPtr = lib.ptr((int) f.getValueArg(arg));
        return lib.valueInt(valuePtrPtr);
    }

    @Override
    public int value_type(Function f, int arg) throws SQLException {
        int valuePtrPtr = lib.ptr((int) f.getValueArg(arg));
        return lib.valueType(valuePtrPtr);
    }

    @Override
    public int create_function(String name, Function f, int nArgs, int flags) throws SQLException {
        int namePtr = lib.allocCString(name);
        int userData = UDFStore.registerFunction(name, f);

        int result;
        if (f instanceof Function.Aggregate) {
            boolean isWindow = f instanceof Function.Window;
            result =
                    lib.createFunctionAggregate(dbPtr(), namePtr, nArgs, flags, userData, isWindow);
        } else {
            result = lib.createFunction(dbPtr(), namePtr, nArgs, flags, userData);
        }
        lib.free(namePtr);
        return result;
    }

    @Override
    public int destroy_function(String name) throws SQLException {
        int namePtr = lib.allocCString(name);
        int result = lib.createNullFunction(dbPtr(), namePtr);
        UDFStore.free(name);
        lib.free(namePtr);
        return result;
    }

    @Override
    public int create_collation(String name, Collation c) throws SQLException {
        int namePtr = lib.allocCString(name);
        int userData = collationStore.registerCollation(name, c);

        int result = lib.createCollation(dbPtr(), namePtr, SQLITE_UTF8, userData);
        lib.free(namePtr);
        return result;
    }

    @Override
    public int destroy_collation(String name) throws SQLException {
        collationStore.free(name);

        int namePtr = lib.allocCString(name);

        int result = lib.destroyCollation(dbPtr(), namePtr);
        lib.free(namePtr);
        return result;
    }

    private static final int DEFAULT_BACKUP_BUSY_SLEEP_TIME_MILLIS = 100;
    private static final int DEFAULT_BACKUP_NUM_BUSY_BEFORE_FAIL = 3;
    private static final int DEFAULT_PAGES_PER_BACKUP_STEP = 100;

    private static final int SQLITE_OPEN_READONLY = 0x00000001; /* Ok for sqlite3_open_v2() */
    private static final int SQLITE_OPEN_READWRITE = 0x00000002; /* Ok for sqlite3_open_v2() */
    private static final int SQLITE_OPEN_CREATE = 0x00000004; /* Ok for sqlite3_open_v2() */
    private static final int SQLITE_OPEN_DELETEONCLOSE = 0x00000008; /* VFS only */
    private static final int SQLITE_OPEN_EXCLUSIVE = 0x00000010; /* VFS only */
    private static final int SQLITE_OPEN_AUTOPROXY = 0x00000020; /* VFS only */
    private static final int SQLITE_OPEN_URI = 0x00000040; /* Ok for sqlite3_open_v2() */
    private static final int SQLITE_OPEN_MEMORY = 0x00000080; /* Ok for sqlite3_open_v2() */

    @Override
    public int backup(String dbName, String destFileName, ProgressObserver observer)
            throws SQLException {
        return this.backup(
                dbName,
                destFileName,
                observer,
                DEFAULT_BACKUP_BUSY_SLEEP_TIME_MILLIS,
                DEFAULT_BACKUP_NUM_BUSY_BEFORE_FAIL,
                DEFAULT_PAGES_PER_BACKUP_STEP);
    }

    @Override
    public int backup(
            String dbName,
            String destFileName,
            ProgressObserver observer,
            int sleepTimeMillis,
            int nTimeoutLimit,
            int pagesPerStep)
            throws SQLException {
        int originNamePtr = lib.allocCString(dbName);
        int destNamePtr = lib.allocCString(destFileName);
        int mainStrPtr = lib.allocCString("main");
        int destDbPtr = lib.malloc(PTR_SIZE);

        int flags = SQLITE_OPEN_READWRITE + SQLITE_OPEN_CREATE;
        if (destFileName.startsWith("file:")) {
            flags += SQLITE_OPEN_URI;
        }

        // TODO: verify why we need this dance around VFS
        Path dest = fs.getPath(destFileName);
        try {
            if (dest.getParent() != null) {
                Files.createDirectories(dest.getParent());
            }
        } catch (FileAlreadyExistsException e) {
            // TODO: review carefully the rest of the usage of createDirectories
            // createDirectories is failing
        } catch (IOException e) {
            throw new SQLiteException(
                    "failed to map to in-memory VFS " + e.getMessage(),
                    SQLiteErrorCode.SQLITE_ERROR);
        }
        try {
            Files.deleteIfExists(dest);
        } catch (IOException e) {
            throw new SQLiteException(
                    "failed to map to in-memory VFS " + e.getMessage(),
                    SQLiteErrorCode.SQLITE_ERROR);
        }

        int rc = lib.openV2(destNamePtr, destDbPtr, flags, 0);
        int nTimeout = 0;
        if (rc == SQLITE_OK) {
            int pBackup = lib.backupInit(lib.ptr(destDbPtr), mainStrPtr, dbPtr(), originNamePtr);
            do {
                rc = lib.backupStep(pBackup, pagesPerStep);

                // if the step completed successfully, update progress
                if (observer != null && (rc == SQLITE_OK || rc == SQLITE_DONE)) {
                    int remaining = lib.backupRemaining(pBackup);
                    int pageCount = lib.backupPageCount(pBackup);
                    observer.progress(remaining, pageCount);
                }

                if (rc == SQLITE_BUSY || rc == SQLITE_LOCKED) {
                    if (nTimeout++ >= nTimeoutLimit) {
                        break;
                    }
                    lib.sleep(sleepTimeMillis);
                }
            } while (rc == SQLITE_OK || rc == SQLITE_BUSY || rc == SQLITE_LOCKED);

            lib.backupFinish(pBackup);
            rc = lib.extendedErrorcode(lib.ptr(destDbPtr));
        }

        lib.free(originNamePtr);
        lib.free(destNamePtr);
        lib.free(destDbPtr);
        lib.free(mainStrPtr);

        // and now copy the backup file from the VFS to the real disk
        Path realDiskDest = Path.of(destFileName);
        try {
            java.nio.file.Files.copy(dest, realDiskDest, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(dest);
        } catch (IOException e) {
            throw new SQLiteException(
                    "failed to map to in-memory VFS " + e.getMessage(),
                    SQLiteErrorCode.SQLITE_ERROR);
        }
        return rc;
    }

    @Override
    public int restore(String dbName, String sourceFileName, ProgressObserver observer)
            throws SQLException {
        return this.restore(
                dbName,
                sourceFileName,
                observer,
                DEFAULT_BACKUP_BUSY_SLEEP_TIME_MILLIS,
                DEFAULT_BACKUP_NUM_BUSY_BEFORE_FAIL,
                DEFAULT_PAGES_PER_BACKUP_STEP);
    }

    @Override
    public int restore(
            String dbName,
            String sourceFileName,
            ProgressObserver observer,
            int sleepTimeMillis,
            int nTimeoutLimit,
            int pagesPerStep)
            throws SQLException {
        int destNamePtr = lib.allocCString(dbName);
        int sourceNamePtr = lib.allocCString(sourceFileName);
        int mainStrPtr = lib.allocCString("main");
        int sourceDbPtr = lib.malloc(PTR_SIZE);

        int flags = SQLITE_OPEN_READONLY;
        if (sourceFileName.startsWith("file:")) {
            flags += SQLITE_OPEN_URI;
        }

        // and now copy the backup file from the VFS to the real disk
        Path realDiskSource = Path.of(sourceFileName);
        Path source = fs.getPath(sourceFileName);
        try {
            Files.createDirectories(source);
            java.nio.file.Files.copy(realDiskSource, source, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SQLiteException(
                    "failed to map to in-memory VFS " + e.getMessage(),
                    SQLiteErrorCode.SQLITE_ERROR);
        }

        int rc = lib.openV2(sourceNamePtr, sourceDbPtr, flags, 0);
        int nTimeout = 0;
        if (rc == SQLITE_OK) {
            int pBackup = lib.backupInit(dbPtr(), destNamePtr, lib.ptr(sourceDbPtr), mainStrPtr);
            do {
                rc = lib.backupStep(pBackup, pagesPerStep);

                // if the step completed successfully, update progress
                if (observer != null && (rc == SQLITE_OK || rc == SQLITE_DONE)) {
                    int remaining = lib.backupRemaining(pBackup);
                    int pageCount = lib.backupPageCount(pBackup);
                    observer.progress(remaining, pageCount);
                }

                if (rc == SQLITE_BUSY || rc == SQLITE_LOCKED) {
                    if (nTimeout++ >= nTimeoutLimit) {
                        break;
                    }
                    lib.sleep(sleepTimeMillis);
                }
            } while (rc == SQLITE_OK || rc == SQLITE_BUSY || rc == SQLITE_LOCKED);

            lib.backupFinish(pBackup);
            rc = lib.extendedErrorcode(lib.ptr(sourceDbPtr));
        }

        lib.free(destNamePtr);
        lib.free(destNamePtr);
        lib.free(sourceDbPtr);
        lib.free(mainStrPtr);

        return rc;
    }

    @Override
    public int limit(int id, int value) throws SQLException {
        return lib.limit(dbPtr(), id, value);
    }

    @Override
    public void register_progress_handler(int vmCalls, ProgressHandler progressHandler)
            throws SQLException {
        int progressHandlerIdx = dbPtr();
        ProgressHandlerStore.registerProgressHandler(progressHandlerIdx, progressHandler);
        lib.progressHandler(dbPtr(), vmCalls, progressHandlerIdx);
    }

    @Override
    public void clear_progress_handler() throws SQLException {
        ProgressHandlerStore.free(dbPtr());
        lib.progressHandler(dbPtr(), 0, 0);
    }

    @Override
    boolean[][] column_metadata(long stmtPtrPtr) throws SQLException {
        int stmtPtr = lib.ptr((int) stmtPtrPtr);
        int colCount = lib.columnCount(stmtPtr);

        boolean[][] result = new boolean[colCount][3];

        for (int i = 0; i < colCount; i++) {
            // load passed column name and table name
            int zColumnNamePtr = lib.columnName(stmtPtr, i);
            int zTableNamePtr = lib.columnTableName(stmtPtr, i);

            int pNotNullPtr = lib.malloc(1);
            int pPrimaryKeyPtr = lib.malloc(1);
            int pAutoincPtr = lib.malloc(1);

            instance.memory().writeByte(pNotNullPtr, (byte) 0);
            instance.memory().writeByte(pPrimaryKeyPtr, (byte) 0);
            instance.memory().writeByte(pAutoincPtr, (byte) 0);

            int res =
                    lib.columnMetadata(
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
            lib.free(pNotNullPtr);
            lib.free(pPrimaryKeyPtr);
            lib.free(pAutoincPtr);
        }
        return result;
    }

    @Override
    void set_commit_listener(boolean enabled) {
        if (enabled) {
            lib.commitHook(this.dbPtr, 0);
            lib.rollbackHook(this.dbPtr, 0);
        } else {
            lib.deleteCommitHook(this.dbPtr);
            lib.deleteRollbackHook(this.dbPtr);
        }
    }

    @Override
    void set_update_listener(boolean enabled) {
        if (enabled) {
            lib.updateHook(this.dbPtr, 0);
        } else {
            lib.deleteUpdateHook(this.dbPtr);
        }
    }

    @Override
    public byte[] serialize(String schema) throws SQLException {
        int schemaPtr = lib.allocCString(schema);
        int sizePtr = lib.malloc(8);

        int buffPtr = lib.serialize(dbPtr(), schemaPtr, sizePtr, SQLITE_SERIALIZE_NOCOPY);
        boolean needFree = false;
        if (buffPtr == 0) {
            // This happens if we start without a deserialized database
            buffPtr = lib.serialize(dbPtr(), schemaPtr, sizePtr, 0);
            needFree = true;
        }

        long buffSize = instance.memory().readLong(sizePtr);

        if (buffSize > Integer.MAX_VALUE || buffSize < 0L) {
            throw new SQLException("Serialized buffer is larger than an integer");
        }

        lib.free(sizePtr);
        lib.free(schemaPtr);

        byte[] result = instance.memory().readBytes(buffPtr, (int) buffSize);
        if (needFree) {
            lib.free(buffPtr);
        }

        return result;
    }

    @Override
    public void deserialize(String schema, byte[] buff) throws SQLException {
        int schemaPtr = lib.allocCString(schema);
        int buffPtr = lib.malloc(buff.length);
        instance.memory().write(buffPtr, buff);

        int res = lib.deserialize(dbPtr(), schemaPtr, buffPtr, buff.length);
        if (res != SQLITE_OK) {
            throw DB.newSQLException(res, errmsg());
        }

        // DO NOT FREE those!
        // exports.free(schemaPtr);
        // exports.free(buffPtr);
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
    // Should we cache something?
    // another alternative is to compute it at compile time ...
    public static String version() {
        WasiOptions wasiOpts = WasiOptions.builder().build();

        try (WasiPreview1 wasiPreview1 = WasiPreview1.builder().withOptions(wasiOpts).build()) {
            Instance tmp =
                    Instance.builder(MODULE)
                            .withMachineFactory(SQLiteModule::create)
                            .withImportValues(
                                    ImportValues.builder()
                                            .addFunction(wasiPreview1.toHostFunctions())
                                            .addFunction(new DummyWasmDBImports().toHostFunctions())
                                            .build())
                            .withStart(false)
                            .build();
            int ptr = new WasmDBExports(tmp).version();

            String version = tmp.memory().readCString(ptr);
            return version;
        }
    }
}
