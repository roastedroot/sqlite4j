package com.github.andreaTP.sqlite.wasm.core;

import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.MessageFormat;

import com.github.andreaTP.sqlite.wasm.wasm.SQLiteModule;
import com.github.andreaTP.sqlite.wasm.wasm.WasmDBExports;
import com.github.andreaTP.sqlite.wasm.BusyHandler;
import com.github.andreaTP.sqlite.wasm.Collation;
import com.github.andreaTP.sqlite.wasm.Function;
import com.github.andreaTP.sqlite.wasm.ProgressHandler;
import com.github.andreaTP.sqlite.wasm.SQLiteConfig;
import com.github.andreaTP.sqlite.wasm.util.Logger;
import com.github.andreaTP.sqlite.wasm.util.LoggerFactory;

public class WasmDB extends DB {
    private static final Logger logger = LoggerFactory.getLogger(WasmDB.class);

    public static final int PTR_SIZE = 4;

    private static final int SQLITE_OK = 0; /* Successful result */
    private static final int SQLITE_ERROR = 1; /* Generic error */
    private static final int SQLITE_INTERNAL = 2; /* Internal logic error in SQLite */
    private static final int SQLITE_PERM = 3; /* Access permission denied */
    private static final int SQLITE_ABORT = 4; /* Callback routine requested an abort */
    private static final int SQLITE_BUSY = 5; /* The database file is locked */
    private static final int SQLITE_LOCKED = 6; /* A table in the database is locked */
    private static final int SQLITE_NOMEM = 7; /* A malloc() failed */
    private static final int SQLITE_READONLY = 8; /* Attempt to write a readonly database */
    private static final int SQLITE_INTERRUPT = 9; /* Operation terminated by sqlite3_interrupt()*/
    private static final int SQLITE_IOERR = 10; /* Some kind of disk I/O error occurred */
    private static final int SQLITE_CORRUPT = 11; /* The database disk image is malformed */
    private static final int SQLITE_NOTFOUND = 12; /* Unknown opcode in sqlite3_file_control() */
    private static final int SQLITE_FULL = 13; /* Insertion failed because database is full */
    private static final int SQLITE_CANTOPEN = 14; /* Unable to open the database file */
    private static final int SQLITE_PROTOCOL = 15; /* Database lock protocol error */
    private static final int SQLITE_EMPTY = 16; /* Internal use only */
    private static final int SQLITE_SCHEMA = 17; /* The database schema changed */
    private static final int SQLITE_TOOBIG = 18; /* String or BLOB exceeds size limit */
    private static final int SQLITE_CONSTRAINT = 19; /* Abort due to constraint violation */
    private static final int SQLITE_MISMATCH = 20; /* Data type mismatch */
    private static final int SQLITE_MISUSE = 21; /* Library used incorrectly */
    private static final int SQLITE_NOLFS = 22; /* Uses OS features not supported on host */
    private static final int SQLITE_AUTH = 23; /* Authorization denied */
    private static final int SQLITE_FORMAT = 24; /* Not used */
    private static final int SQLITE_RANGE = 25; /* 2nd parameter to sqlite3_bind out of range */
    private static final int SQLITE_NOTADB = 26; /* File opened that is not a database file */
    private static final int SQLITE_NOTICE = 27; /* Notifications from sqlite3_log() */
    private static final int SQLITE_WARNING = 28; /* Warnings from sqlite3_log() */
    private static final int SQLITE_ROW = 100; /* sqlite3_step() has another row ready */
    private static final int SQLITE_DONE = 101; /* sqlite3_step() has finished executing */

    private static final int SQLITE_TRANSIENT = -1; // ???

    private final Instance instance;
    private final WasiPreview1 wasiPreview1;
    private final WasmDBExports exports;

    /** SQLite connection handle. */
    private int dbPtrPtr = 0;
    private int dbPtr = 0;

    public WasmDB(String url, String fileName, SQLiteConfig config) throws SQLException {
        super(url, fileName, config);
        WasiOptions wasiOpts = WasiOptions.builder().inheritSystem().build();
        this.wasiPreview1 = WasiPreview1.builder().withOptions(wasiOpts).build();
        this.instance =
                Instance.builder(SQLiteModule.load())
                        .withMachineFactory(SQLiteModule::create)
                        .withImportValues(
                                ImportValues.builder()
                                        .addFunction(wasiPreview1.toHostFunctions())
                                        .build())
                        .build();
        this.exports = new WasmDBExports(instance);
    }

    // safe access to the dbPointer
    private int dbPtr() {
        if (this.dbPtrPtr == 0 || this.dbPtr == 0) {
            throw new RuntimeException(new SQLException("Attempting to perform operations on a database not opened"));
        }
        return this.dbPtr;
    }

    @Override
    protected void _open(String filename, int openFlags) throws SQLException {
        if (!filename.equals(":memory:")) {
            // TODO: fixme!
            throw new RuntimeException("opening a db from file is not yet supported.");
        }

        this.dbPtrPtr = exports.malloc(PTR_SIZE);
        int dbNamePtr = exports.allocCString(filename);

        int res = exports.openV2(dbNamePtr, dbPtrPtr, openFlags, 0);
        exports.free(dbNamePtr);
        if (res != SQLITE_OK) {
            throw new SQLException("Failed to open database " + filename + " error code: " + res);
        }

        this.dbPtr = instance.memory().readInt(this.dbPtrPtr);
    }

    @Override
    protected SafeStmtPtr prepare(String sql) throws SQLException {
        logger.trace(
                () ->
                        MessageFormat.format(
                                "DriverManager [{0}] [SQLite EXEC] {1}",
                                Thread.currentThread().getName(), sql));
        int stmtPtrPtr = exports.malloc(PTR_SIZE);
        int sqlBytesPtr = exports.allocCString(sql);

        exports.prepareV2(dbPtr, sqlBytesPtr, sql.length(), stmtPtrPtr, 0);
        exports.free(sqlBytesPtr);

        return new SafeStmtPtr(this, stmtPtr);
    }

    @Override
    protected int finalize(long stmt) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3Finalize(stmtPtrPtr);
    }

    @Override
    public int step(long stmt) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3Step(stmtPtrPtr);
    }

    @Override
    public int _exec(String sql) throws SQLException {
        String sqlBytes = new String(sql.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        int sqlBytesPtr = malloc(sqlBytes.length());
        instance.memory().writeCString(sqlBytesPtr, sqlBytes);

        int status = exports.sqlite3Exec(pointer, sqlBytesPtr, 0, 0, 0);
        free(sqlBytesPtr);
        if (status != SQLITE_OK) {
            throw new SQLException("Failed to exec " + sql);
        }

        return status;
    }

    @Override
    public long changes() throws SQLException {
        return exports.sqlite3Changes64(pointer);
    }

    @Override
    public void interrupt() throws SQLException {
        throw new RuntimeException("interrupt not implemented in WasmDB");
    }

    @Override
    public void busy_timeout(int ms) throws SQLException {
        // throw new RuntimeException("busy_timeout not implemented in WasmDB");
        // TODO: implement me skip for now
    }

    @Override
    public void busy_handler(BusyHandler busyHandler) throws SQLException {
        throw new RuntimeException("busy_handler not implemented in WasmDB");
    }

    @Override
    String errmsg() throws SQLException {
        throw new RuntimeException("errmsg not implemented in WasmDB");
    }

    @Override
    public String libversion() throws SQLException {
        throw new RuntimeException("libversion not implemented in WasmDB");
    }

    @Override
    public long total_changes() throws SQLException {
        return exports.sqlite3TotalChanges(pointer);
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
        exports.sqlite3Close(pointer);
    }

    @Override
    public int reset(long stmt) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3Reset(stmtPtrPtr);
    }

    @Override
    public int clear_bindings(long stmt) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3ClearBindings(stmtPtrPtr);
    }

    @Override
    int bind_parameter_count(long stmt) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3BindParameterCount(stmtPtrPtr);
    }

    @Override
    public int column_count(long stmt) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3ColumnCount(stmtPtrPtr);
    }

    @Override
    public int column_type(long stmt, int col) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3ColumnType(stmtPtrPtr, col);
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
    public String column_name(long stmt, int col) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        int columnNamePtr = exports.sqlite3ColumnName(stmtPtrPtr, col);
        if (columnNamePtr == 0) {
            return null;
        }
        return instance.memory().readCString(columnNamePtr);
    }

    @Override
    public String column_text(long stmt, int col) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        int txtPtr = exports.sqlite3ColumnText(stmtPtrPtr, col);
        String result = instance.memory().readCString(txtPtr);
        free(txtPtr);
        return result;
    }

    @Override
    public byte[] column_blob(long stmt, int col) throws SQLException {
        throw new RuntimeException("column_blob not implemented in WasmDB");
    }

    @Override
    public double column_double(long stmt, int col) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3ColumnDouble(stmtPtrPtr, col);
    }

    @Override
    public long column_long(long stmt, int col) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3ColumnInt64(stmtPtrPtr, col);
    }

    @Override
    public int column_int(long stmt, int col) throws SQLException {
        throw new RuntimeException("column_int not implemented in WasmDB");
    }

    @Override
    int bind_null(long stmt, int pos) throws SQLException {
        throw new RuntimeException("bind_null not implemented in WasmDB");
    }

    @Override
    int bind_int(long stmt, int pos, int v) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3BindInt(stmtPtrPtr, pos, v);
    }

    @Override
    int bind_long(long stmt, int pos, long v) throws SQLException {
        throw new RuntimeException("bind_long not implemented in WasmDB");
    }

    @Override
    int bind_double(long stmt, int pos, double v) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        return exports.sqlite3BindDouble(stmtPtrPtr, pos, v);
    }

    @Override
    int bind_text(long stmt, int pos, String v) throws SQLException {
        int stmtPtrPtr = instance.memory().readInt((int) stmt);
        // TODO: doublecheck allocation of Strings: should it be +1 byte for the terminal 0?
        int vPtr = malloc(v.length());
        instance.memory()
                .writeCString(
                        vPtr,
                        new String(v.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        int result = exports.sqlite3BindText(stmtPtrPtr, pos, vPtr, v.length(), SQLITE_TRANSIENT);
        free(vPtr);

        return result;
    }

    @Override
    int bind_blob(long stmt, int pos, byte[] v) throws SQLException {
        throw new RuntimeException("bind_blob not implemented in WasmDB");
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
        return exports.sqlite3Limit(pointer, id, value);
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
