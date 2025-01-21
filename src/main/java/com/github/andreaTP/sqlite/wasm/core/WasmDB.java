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
import java.util.Locale;

public class WasmDB extends DB {
    private static final Logger logger = LoggerFactory.getLogger(WasmDB.class);

    public static final int PTR_SIZE = 4;

    private final Instance instance;
    private final WasiPreview1 wasiPreview1;
    private final WasmDBExports exports;

    // TODO: double-check proper cleanup of resources
    private final FileSystem fs;
    private final String dbFileName;

    /** SQLite connection handle. */
    private int dbPtrPtr = 0;
    private int dbPtr = 0;

    public WasmDB(String url, String fileName, SQLiteConfig config) throws SQLException {
        super(url, fileName, config);
        if (!fileName.equals(":memory:")) {
            this.fs =
                    Jimfs.newFileSystem(
                            Configuration.unix().toBuilder().setAttributeViews("unix").build());
            Path target = fs.getPath("tmp");
            try {
                java.nio.file.Files.createDirectory(target);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory on the in-memory fs", e);
            }
            this.dbFileName = Path.of(fileName).toFile().getName();

            WasiOptions wasiOpts =
                    WasiOptions.builder()
                            .inheritSystem()
                            .withDirectory(target.toString(), target)
                            .build();
            this.wasiPreview1 = WasiPreview1.builder().withOptions(wasiOpts).build();
            this.instance =
                    Instance.builder(SQLiteModule.load())
                            .withMachineFactory(SQLiteModule::create)
                            .withImportValues(
                                    ImportValues.builder()
                                            .addFunction(wasiPreview1.toHostFunctions())
                                            .build())
                            .withMemoryLimits(new MemoryLimits(10, MemoryLimits.MAX_PAGES))
                            .build();
        } else {
            this.fs = null;
            this.dbFileName = null;

            WasiOptions wasiOpts = WasiOptions.builder().inheritSystem().build();
            this.wasiPreview1 = WasiPreview1.builder().withOptions(wasiOpts).build();
            this.instance =
                    Instance.builder(SQLiteModule.load())
                            .withMachineFactory(SQLiteModule::create)
                            .withImportValues(
                                    ImportValues.builder()
                                            .addFunction(wasiPreview1.toHostFunctions())
                                            .build())
                            .withMemoryLimits(new MemoryLimits(10, MemoryLimits.MAX_PAGES))
                            .build();
        }

        this.exports = new WasmDBExports(instance);
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
            try (InputStream is = new FileInputStream(filename)) {
                java.nio.file.Files.copy(
                        is,
                        fs.getPath("tmp").resolve(this.dbFileName),
                        StandardCopyOption.REPLACE_EXISTING);
                filename = fs.getPath("tmp").resolve(this.dbFileName).toString();
            } catch (IOException e) {
                throw new SQLException("Failed to map to memory the file: " + filename);
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
        logger.trace(
                () ->
                        MessageFormat.format(
                                "DriverManager [{0}] [SQLite EXEC] {1}",
                                Thread.currentThread().getName(), sql));
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
        exports.close(dbPtr());
        exports.free(dbPtrPtr);
        if (this.fs != null) {
            try {
                this.fs.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
        exports.free(txtPtr);
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
        return exports.limit(dbPtr(), id, value);
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
