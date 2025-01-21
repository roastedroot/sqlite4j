package com.github.andreaTP.sqlite.wasm.core;

import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import com.github.andreaTP.sqlite.wasm.BusyHandler;
import com.github.andreaTP.sqlite.wasm.Collation;
import com.github.andreaTP.sqlite.wasm.Function;
import com.github.andreaTP.sqlite.wasm.ProgressHandler;
import com.github.andreaTP.sqlite.wasm.SQLiteConfig;
import com.github.andreaTP.sqlite.wasm.SQLiteModule;
import com.github.andreaTP.sqlite.wasm.util.Logger;
import com.github.andreaTP.sqlite.wasm.util.LoggerFactory;
import com.github.andreaTP.sqlite.wasm.wasm.WasmDBExports;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.MessageFormat;

public class WasmDB extends DB {
    private static final Logger logger = LoggerFactory.getLogger(WasmDB.class);

    public static final int PTR_SIZE = 4;

    private static final Instance INSTANCE;
    private static final WasiPreview1 WASI_PREVIEW_1;
    private static final WasmDBExports EXPORTS;

    // TODO: double-check proper cleanup of resources
    private static final FileSystem FS;

    static {
        // TODO: should this logic go to: loadSQLiteNativeLibrary ?
        FS =
                Jimfs.newFileSystem(
                        Configuration.unix().toBuilder().setAttributeViews("unix").build());
        Path target = FS.getPath("tmp");
        try {
            java.nio.file.Files.createDirectory(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory on the in-memory fs", e);
        }

        WasiOptions wasiOpts =
                WasiOptions.builder()
                        .inheritSystem()
                        .withDirectory(target.toString(), target)
                        .build();
        WASI_PREVIEW_1 = WasiPreview1.builder().withOptions(wasiOpts).build();
        INSTANCE =
                Instance.builder(SQLiteModule.load())
                        .withMachineFactory(SQLiteModule::create)
                        .withImportValues(
                                ImportValues.builder()
                                        .addFunction(WASI_PREVIEW_1.toHostFunctions())
                                        .build())
                        .withMemoryLimits(new MemoryLimits(10, MemoryLimits.MAX_PAGES))
                        .build();
        EXPORTS = new WasmDBExports(INSTANCE);
    }

    public static String version() {
        int ptr = EXPORTS.version();
        String version = INSTANCE.memory().readCString(ptr);
        return version;
    }

    private final String dbFileName;

    /** SQLite connection handle. */
    private int dbPtrPtr = 0;

    private int dbPtr = 0;

    public WasmDB(String url, String fileName, SQLiteConfig config) throws SQLException {
        super(url, fileName, config);
        this.dbFileName = Path.of(fileName).toFile().getName();
    }

    // safe access to the dbPointer
    private int dbPtr() throws SQLException {
        if (this.dbPtrPtr == 0 || this.dbPtr == 0) {
            throw new SQLException("Attempting to perform operations on a database not opened");
        }
        return this.dbPtr;
    }

    @Override
    protected void _open(String filename, int openFlags) throws SQLException {
        if (new File(filename).exists() && !filename.isEmpty()) {
            Path dest = FS.getPath("tmp").resolve(this.dbFileName);
            try (InputStream is = new FileInputStream(filename)) {
                java.nio.file.Files.copy(
                        is,
                        FS.getPath("tmp").resolve(this.dbFileName),
                        StandardCopyOption.REPLACE_EXISTING);
                filename = dest.toString();
            } catch (IOException e) {
                throw new SQLException("Failed to map to memory the file: " + filename);
            }
        }

        this.dbPtrPtr = EXPORTS.malloc(PTR_SIZE);
        int dbNamePtr = EXPORTS.allocCString(filename).ptr();

        int res = EXPORTS.openV2(dbNamePtr, dbPtrPtr, openFlags, 0);
        this.dbPtr = INSTANCE.memory().readInt(this.dbPtrPtr);
        if (res != SQLITE_OK) {
            int errCode = EXPORTS.extendedErrorcode(dbPtr);
            String errmsg = errmsg();
            EXPORTS.close(dbPtr);
            throw new SQLException(
                    "Failed to open database "
                            + filename
                            + ", error code: "
                            + errCode
                            + ", error message: "
                            + errmsg);
        }
        EXPORTS.free(dbNamePtr);
    }

    @Override
    protected SafeStmtPtr prepare(String sql) throws SQLException {
        logger.trace(
                () ->
                        MessageFormat.format(
                                "DriverManager [{0}] [SQLite EXEC] {1}",
                                Thread.currentThread().getName(), sql));
        int stmtPtrPtr = EXPORTS.malloc(PTR_SIZE);
        WasmDBExports.StringPtrSize str = EXPORTS.allocCString(sql);

        EXPORTS.prepareV2(dbPtr(), str.ptr(), str.size(), stmtPtrPtr, 0);
        EXPORTS.free(str.ptr());

        return new SafeStmtPtr(this, stmtPtrPtr);
    }

    @Override
    protected int finalize(long stmtPtrPtr) throws SQLException {
        int result = EXPORTS.finalize(EXPORTS.ptr((int) stmtPtrPtr));
        EXPORTS.free((int) stmtPtrPtr);
        return result;
    }

    @Override
    public int step(long stmtPtrPtr) throws SQLException {
        return EXPORTS.step(EXPORTS.ptr((int) stmtPtrPtr));
    }

    @Override
    public int _exec(String sql) throws SQLException {
        int sqlBytesPtr = EXPORTS.allocCString(sql).ptr();

        int status = EXPORTS.exec(dbPtr(), sqlBytesPtr, 0, 0, 0);
        EXPORTS.free(sqlBytesPtr);
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
        return EXPORTS.changes(dbPtr());
    }

    @Override
    public void interrupt() throws SQLException {
        throw new RuntimeException("interrupt not implemented in WasmDB");
    }

    @Override
    public void busy_timeout(int ms) throws SQLException {
        EXPORTS.busyTimeout(dbPtr(), ms);
    }

    @Override
    public void busy_handler(BusyHandler busyHandler) throws SQLException {
        throw new RuntimeException("busy_handler not implemented in WasmDB");
    }

    @Override
    String errmsg() throws SQLException {
        int errPtr = EXPORTS.errmsg(dbPtr());
        String err = INSTANCE.memory().readCString(errPtr);
        return err;
    }

    @Override
    public String libversion() throws SQLException {
        throw new RuntimeException("libversion not implemented in WasmDB");
    }

    @Override
    public long total_changes() throws SQLException {
        return EXPORTS.totalChanges(dbPtr());
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
        EXPORTS.close(dbPtr());
        EXPORTS.free(dbPtrPtr);
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
        return EXPORTS.reset(EXPORTS.ptr((int) stmtPtrPtr));
    }

    @Override
    public int clear_bindings(long stmtPtrPtr) throws SQLException {
        return EXPORTS.clearBindings(EXPORTS.ptr((int) stmtPtrPtr));
    }

    @Override
    int bind_parameter_count(long stmtPtrPtr) throws SQLException {
        return EXPORTS.bindParameterCount(EXPORTS.ptr((int) stmtPtrPtr));
    }

    @Override
    public int column_count(long stmtPtrPtr) throws SQLException {
        return EXPORTS.columnCount(EXPORTS.ptr((int) stmtPtrPtr));
    }

    @Override
    public int column_type(long stmtPtrPtr, int col) throws SQLException {
        return EXPORTS.columnType(EXPORTS.ptr((int) stmtPtrPtr), col);
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
        int columnNamePtr = EXPORTS.columnName(EXPORTS.ptr((int) stmtPtrPtr), col);
        if (columnNamePtr == 0) {
            return null;
        }
        return INSTANCE.memory().readCString(columnNamePtr);
    }

    @Override
    public String column_text(long stmtPtrPtr, int col) throws SQLException {
        int stmtPtr = EXPORTS.ptr((int) stmtPtrPtr);
        int txtPtr = EXPORTS.columnText(stmtPtr, col);
        int txtLength = EXPORTS.columnBytes(stmtPtr, col);
        if (txtPtr == 0) {
            return null;
        }

        byte[] bytes = INSTANCE.memory().readBytes(txtPtr, txtLength);
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
        return EXPORTS.columnBlob(EXPORTS.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public double column_double(long stmtPtrPtr, int col) throws SQLException {
        return EXPORTS.columnDouble(EXPORTS.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public long column_long(long stmtPtrPtr, int col) throws SQLException {
        return EXPORTS.columnLong(EXPORTS.ptr((int) stmtPtrPtr), col);
    }

    @Override
    public int column_int(long stmtPtrPtr, int col) throws SQLException {
        return EXPORTS.columnInt(EXPORTS.ptr((int) stmtPtrPtr), col);
    }

    @Override
    int bind_null(long stmtPtrPtr, int pos) throws SQLException {
        return EXPORTS.bindNull(EXPORTS.ptr((int) stmtPtrPtr), pos);
    }

    @Override
    int bind_int(long stmtPtrPtr, int pos, int v) throws SQLException {
        return EXPORTS.bindInt(EXPORTS.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_long(long stmtPtrPtr, int pos, long v) throws SQLException {
        return EXPORTS.bindLong(EXPORTS.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_double(long stmtPtrPtr, int pos, double v) throws SQLException {
        return EXPORTS.bindDouble(EXPORTS.ptr((int) stmtPtrPtr), pos, v);
    }

    @Override
    int bind_text(long stmtPtrPtr, int pos, String v) throws SQLException {
        WasmDBExports.StringPtrSize str = EXPORTS.allocCString(v);
        int result = EXPORTS.bindText(EXPORTS.ptr((int) stmtPtrPtr), pos, str.ptr(), str.size());
        EXPORTS.free(str.ptr());
        return result;
    }

    @Override
    int bind_blob(long stmtPtrPtr, int pos, byte[] v) throws SQLException {
        int blobPtr = EXPORTS.malloc(v.length);
        INSTANCE.memory().write(blobPtr, v);
        int result = EXPORTS.bindBlob(EXPORTS.ptr((int) stmtPtrPtr), pos, blobPtr, v.length);
        EXPORTS.free(blobPtr);
        return result;
    }

    @Override
    public void result_null(long context) throws SQLException {
        throw new RuntimeException("result_null not implemented in WasmDB");
    }

    @Override
    public void result_text(long context, String val) throws SQLException {
        throw new RuntimeException("result_text not implemented in WasmDB");
    }

    @Override
    public void result_blob(long context, byte[] val) throws SQLException {
        throw new RuntimeException("result_blob not implemented in WasmDB");
    }

    @Override
    public void result_double(long context, double val) throws SQLException {
        throw new RuntimeException("result_double not implemented in WasmDB");
    }

    @Override
    public void result_long(long context, long val) throws SQLException {
        throw new RuntimeException("result_long not implemented in WasmDB");
    }

    @Override
    public void result_int(long context, int val) throws SQLException {
        throw new RuntimeException("result_int not implemented in WasmDB");
    }

    @Override
    public void result_error(long context, String err) throws SQLException {
        throw new RuntimeException("result_error not implemented in WasmDB");
    }

    @Override
    public String value_text(Function f, int arg) throws SQLException {
        throw new RuntimeException("value_text not implemented in WasmDB");
    }

    @Override
    public byte[] value_blob(Function f, int arg) throws SQLException {
        throw new RuntimeException("value_blob not implemented in WasmDB");
    }

    @Override
    public double value_double(Function f, int arg) throws SQLException {
        throw new RuntimeException("value_double not implemented in WasmDB");
    }

    @Override
    public long value_long(Function f, int arg) throws SQLException {
        throw new RuntimeException("value_long not implemented in WasmDB");
    }

    @Override
    public int value_int(Function f, int arg) throws SQLException {
        throw new RuntimeException("value_int not implemented in WasmDB");
    }

    @Override
    public int value_type(Function f, int arg) throws SQLException {
        throw new RuntimeException("value_type not implemented in WasmDB");
    }

    @Override
    public int create_function(String name, Function f, int nArgs, int flags) throws SQLException {
        throw new RuntimeException("create_function not implemented in WasmDB");
    }

    @Override
    public int destroy_function(String name) throws SQLException {
        throw new RuntimeException("destroy_function not implemented in WasmDB");
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
        return EXPORTS.limit(dbPtr(), id, value);
    }

    @Override
    public void register_progress_handler(int vmCalls, ProgressHandler progressHandler)
            throws SQLException {
        throw new RuntimeException("register_progress_handler not implemented in WasmDB");
    }

    @Override
    public void clear_progress_handler() throws SQLException {
        throw new RuntimeException("clear_progress_handler not implemented in WasmDB");
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
}
