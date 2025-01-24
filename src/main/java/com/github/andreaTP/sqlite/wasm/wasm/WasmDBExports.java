package com.github.andreaTP.sqlite.wasm.wasm;

import static com.github.andreaTP.sqlite.wasm.core.Codes.SQLITE_NULL;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.types.Value;
import java.nio.charset.StandardCharsets;

// Manually writing it to avoid passing through the Map lookup on every invocation
public class WasmDBExports {

    private static final int SQLITE_TRANSIENT = -1; // https://www.sqlite.org/c3ref/c_static.html

    private static final int SQLITE_UTF8 = 1; /* IMP: R-37514-35566 */
    private static final int SQLITE_UTF16LE = 2; /* IMP: R-03371-37637 */
    private static final int SQLITE_UTF16BE = 3; /* IMP: R-51971-34154 */
    private static final int SQLITE_UTF16 = 4; /* Use native byte order */
    private static final int SQLITE_ANY = 5; /* Deprecated */
    private static final int SQLITE_UTF16_ALIGNED = 8; /* sqlite3_create_collation only */

    private final Instance instance;
    private final ExportFunction realloc;
    private final ExportFunction malloc;
    private final ExportFunction free;
    private final ExportFunction openV2;
    private final ExportFunction prepareV2;
    private final ExportFunction finalize;
    private final ExportFunction step;
    private final ExportFunction exec;
    private final ExportFunction changes;
    private final ExportFunction totalChanges;
    private final ExportFunction close;
    private final ExportFunction reset;
    private final ExportFunction clearBindings;
    private final ExportFunction bindParameterCount;
    private final ExportFunction columnCount;
    private final ExportFunction columnType;
    private final ExportFunction columnName;
    private final ExportFunction columnText;
    private final ExportFunction columnInt;
    private final ExportFunction columnDouble;
    private final ExportFunction columnLong;
    private final ExportFunction columnBlob;
    private final ExportFunction columnBytes;
    private final ExportFunction bindInt;
    private final ExportFunction bindLong;
    private final ExportFunction bindDouble;
    private final ExportFunction bindNull;
    private final ExportFunction bindText;
    private final ExportFunction bindBlob;
    private final ExportFunction limit;
    private final ExportFunction errmsg;
    private final ExportFunction extendedErrcode;
    private final ExportFunction busyTimeout;
    private final ExportFunction version;
    private final ExportFunction createFunction;
    private final ExportFunction createFunctionAggregate;
    private final ExportFunction userData;
    private final ExportFunction resultText;
    private final ExportFunction resultLong;
    private final ExportFunction resultInt;
    private final ExportFunction resultDouble;
    private final ExportFunction resultBlob;
    private final ExportFunction valueDouble;
    private final ExportFunction valueText;
    private final ExportFunction valueInt;
    private final ExportFunction valueBlob;
    private final ExportFunction valueBytes;
    private final ExportFunction valueLong;
    private final ExportFunction progressHandler;
    private final ExportFunction busyHandler;

    private final int xFuncPtr;
    private final int xStepPtr;
    private final int xFinalPtr;
    private final int xValuePtr;
    private final int xInversePtr;
    private final int xDestroyPtr;
    private final int xProgressPtr;
    private final int xBusyPtr;

    public WasmDBExports(Instance instance) {
        this.instance = instance;

        this.xFuncPtr = (int) instance.exports().function("xFuncPtr").apply()[0];
        this.xStepPtr = (int) instance.exports().function("xStepPtr").apply()[0];
        this.xFinalPtr = (int) instance.exports().function("xFinalPtr").apply()[0];
        this.xValuePtr = (int) instance.exports().function("xValuePtr").apply()[0];
        this.xInversePtr = (int) instance.exports().function("xInversePtr").apply()[0];
        this.xDestroyPtr = (int) instance.exports().function("xDestroyPtr").apply()[0];
        this.xProgressPtr = (int) instance.exports().function("xProgressPtr").apply()[0];
        this.xBusyPtr = (int) instance.exports().function("xBusyPtr").apply()[0];

        this.realloc = instance.exports().function("realloc");
        this.malloc = instance.exports().function("malloc");
        this.free = instance.exports().function("free");
        this.openV2 = instance.exports().function("sqlite3_open_v2");
        this.prepareV2 = instance.exports().function("sqlite3_prepare_v2");
        this.finalize = instance.exports().function("sqlite3_finalize");
        this.step = instance.exports().function("sqlite3_step");
        this.exec = instance.exports().function("sqlite3_exec");
        this.changes = instance.exports().function("sqlite3_changes64");
        this.totalChanges = instance.exports().function("sqlite3_total_changes");
        this.close = instance.exports().function("sqlite3_close");
        this.reset = instance.exports().function("sqlite3_reset");
        this.clearBindings = instance.exports().function("sqlite3_clear_bindings");
        this.bindParameterCount = instance.exports().function("sqlite3_bind_parameter_count");
        this.columnCount = instance.exports().function("sqlite3_column_count");
        this.columnType = instance.exports().function("sqlite3_column_type");
        this.columnName = instance.exports().function("sqlite3_column_name");
        this.columnText = instance.exports().function("sqlite3_column_text");
        this.columnInt = instance.exports().function("sqlite3_column_int");
        this.columnDouble = instance.exports().function("sqlite3_column_double");
        this.columnLong = instance.exports().function("sqlite3_column_int64");
        this.columnBlob = instance.exports().function("sqlite3_column_blob");
        this.columnBytes = instance.exports().function("sqlite3_column_bytes");
        this.bindInt = instance.exports().function("sqlite3_bind_int");
        this.bindLong = instance.exports().function("sqlite3_bind_int64");
        this.bindDouble = instance.exports().function("sqlite3_bind_double");
        this.bindNull = instance.exports().function("sqlite3_bind_null");
        this.bindText = instance.exports().function("sqlite3_bind_text");
        this.bindBlob = instance.exports().function("sqlite3_bind_blob");
        this.limit = instance.exports().function("sqlite3_limit");
        this.errmsg = instance.exports().function("sqlite3_errmsg");
        this.extendedErrcode = instance.exports().function("sqlite3_extended_errcode");
        this.busyTimeout = instance.exports().function("sqlite3_busy_timeout");
        this.version = instance.exports().function("sqlite3_libversion");
        this.createFunction = instance.exports().function("sqlite3_create_function_v2");
        this.createFunctionAggregate =
                instance.exports().function("sqlite3_create_window_function");
        this.userData = instance.exports().function("sqlite3_user_data");
        this.resultText = instance.exports().function("sqlite3_result_text");
        this.resultLong = instance.exports().function("sqlite3_result_int64");
        this.resultInt = instance.exports().function("sqlite3_result_int");
        this.resultDouble = instance.exports().function("sqlite3_result_double");
        this.resultBlob = instance.exports().function("sqlite3_result_blob");
        this.valueDouble = instance.exports().function("sqlite3_value_double");
        this.valueText = instance.exports().function("sqlite3_value_text");
        this.valueInt = instance.exports().function("sqlite3_value_int");
        this.valueLong = instance.exports().function("sqlite3_value_int64");
        this.valueBlob = instance.exports().function("sqlite3_value_blob");
        this.valueBytes = instance.exports().function("sqlite3_value_bytes");

        this.progressHandler = instance.exports().function("sqlite3_progress_handler");
        this.busyHandler = instance.exports().function("sqlite3_busy_handler");
    }

    public int malloc(int size) {
        return (int) malloc.apply(size)[0];
    }

    public void free(int ptr) {
        free.apply(ptr);
    }

    public int ptr(int ptrptr) {
        return instance.memory().readInt(ptrptr);
    }

    public static class StringPtrSize {
        private final int ptr;
        private final int size;

        StringPtrSize(int ptr, int size) {
            this.ptr = ptr;
            this.size = size;
        }

        public int ptr() {
            return ptr;
        }

        public int size() {
            return size;
        }
    }

    public StringPtrSize allocCString(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        byte[] strBytes = new byte[bytes.length + 1];
        int strBytesPtr = malloc(strBytes.length);
        System.arraycopy(bytes, 0, strBytes, 0, bytes.length);
        strBytes[bytes.length] = '\0';
        instance.memory().write(strBytesPtr, strBytes);
        return new StringPtrSize(strBytesPtr, strBytes.length);
    }

    //    const char *filename,   /* Database filename (UTF-8) */
    //    sqlite3 **ppDb,         /* OUT: SQLite db handle */
    //    int flags,              /* Flags */
    //    const char *zVfs        /* Name of VFS module to use */
    public int openV2(int filenamePtr, /* OUT */ long dbPtrPtr, int flags, int zVfs) {
        return (int) openV2.apply(filenamePtr, dbPtrPtr, flags, zVfs)[0];
    }

    //    sqlite3 *db,            /* Database handle */
    //    const char *zSql,       /* SQL statement, UTF-8 encoded */
    //    int nByte,              /* Maximum length of zSql in bytes. */
    //    sqlite3_stmt **ppStmt,  /* OUT: Statement handle */
    //    const char **pzTail     /* OUT: Pointer to unused portion of zSql */
    public int prepareV2(
            int dbPtr, int zSql, int nByte, /* OUT */ int stmtPtrPtr, /* OUT */ int pzTail) {
        return (int) prepareV2.apply(dbPtr, zSql, nByte, stmtPtrPtr, pzTail)[0];
    }

    //    sqlite3*,                                  /* An open database */
    //    const char *sql,                           /* SQL to be evaluated */
    //    int (*callback)(void*,int,char**,char**),  /* Callback function */
    //    void *,                                    /* 1st argument to callback */
    //    char **errmsg                              /* Error msg written here */
    public int exec(int dbPtr, int sqlPtr, int callback, int callbackArg0, int errPtr) {
        return (int) exec.apply(dbPtr, sqlPtr, callback, callbackArg0, errPtr)[0];
    }

    public int extendedErrorcode(int dbPtr) {
        return (int) extendedErrcode.apply(dbPtr)[0];
    }

    public int finalize(int stmtPtr) {
        return (int) finalize.apply(stmtPtr)[0];
    }

    public int step(int stmtPtr) {
        return (int) step.apply(stmtPtr)[0];
    }

    public int close(int dbPtr) {
        return (int) close.apply(dbPtr)[0];
    }

    public int limit(int dbPtr, int id, int value) {
        return (int) limit.apply(dbPtr, id, value)[0];
    }

    public int totalChanges(int dbPtr) {
        return (int) totalChanges.apply(dbPtr)[0];
    }

    public long changes(int dbPtr) {
        return changes.apply(dbPtr)[0];
    }

    public int reset(int stmtPtr) {
        return (int) reset.apply(stmtPtr)[0];
    }

    public int clearBindings(int stmtPtr) {
        return (int) clearBindings.apply(stmtPtr)[0];
    }

    public int bindParameterCount(int stmtPtr) {
        return (int) bindParameterCount.apply(stmtPtr)[0];
    }

    public int columnCount(int stmtPtr) {
        return (int) columnCount.apply(stmtPtr)[0];
    }

    public int columnType(int stmtPtr, int col) {
        return (int) columnType.apply(stmtPtr, col)[0];
    }

    public int columnName(int stmtPtr, int col) {
        return (int) columnName.apply(stmtPtr, col)[0];
    }

    public int columnText(int stmtPtr, int col) {
        return (int) columnText.apply(stmtPtr, col)[0];
    }

    public int columnBytes(int stmtPtr, int col) {
        return (int) columnBytes.apply(stmtPtr, col)[0];
    }

    public int columnInt(int stmtPtr, int col) {
        return (int) columnInt.apply(stmtPtr, col)[0];
    }

    public double columnDouble(int stmtPtr, int col) {
        return Value.longToDouble(columnDouble.apply(stmtPtr, col)[0]);
    }

    public long columnLong(int stmtPtr, int col) {
        return columnLong.apply(stmtPtr, col)[0];
    }

    public byte[] columnBlob(int stmtPtr, int col) {
        int type = columnType(stmtPtr, col);
        int blobPtr = (int) columnBlob.apply(stmtPtr, col)[0];
        if (blobPtr == 0) {
            if (type == SQLITE_NULL) {
                return null;
            } else {
                return new byte[] {};
            }
        }

        int length = (int) columnBytes.apply(stmtPtr, col)[0];
        return instance.memory().readBytes(blobPtr, length);
    }

    public int bindInt(int stmtPtr, int pos, int v) {
        return (int) bindInt.apply(stmtPtr, pos, v)[0];
    }

    public int bindLong(int stmtPtr, int pos, long v) {
        return (int) bindLong.apply(stmtPtr, pos, v)[0];
    }

    public int bindDouble(int stmtPtr, int pos, double v) {
        return (int) bindDouble.apply(stmtPtr, pos, Value.doubleToLong(v))[0];
    }

    public int bindNull(int stmtPtr, int pos) {
        return (int) bindNull.apply(stmtPtr, pos)[0];
    }

    public int bindText(int stmtPtr, int pos, int vPtr, int vLength) {
        return (int) bindText.apply(stmtPtr, pos, vPtr, vLength, SQLITE_TRANSIENT)[0];
    }

    public int bindBlob(int stmtPtr, int pos, int vPtr, int vLength) {
        return (int) bindBlob.apply(stmtPtr, pos, vPtr, vLength, SQLITE_TRANSIENT)[0];
    }

    public int errmsg(int dbPtr) {
        return (int) errmsg.apply(dbPtr)[0];
    }

    public int busyTimeout(int dbPtr, int ms) {
        return (int) busyTimeout.apply(dbPtr, ms)[0];
    }

    public int version() {
        return (int) version.apply()[0];
    }

    //    gethandle(env, nativeDB),
    //    name_bytes,            // function name
    //    nArgs,                 // number of args
    //    SQLITE_UTF16 | flags,  // preferred chars
    //    udf,
    //    &xFunc,
    //    NULL,
    //    NULL,
    //    &free_udf_func         // Cleanup function
    public int createFunction(int dbPtr, int namePtr, int nArgs, int flags, int userData) {
        return (int)
                createFunction
                        .apply(
                                dbPtr,
                                namePtr,
                                nArgs,
                                SQLITE_UTF16 | flags,
                                userData,
                                xFuncPtr,
                                0,
                                0,
                                xDestroyPtr)[0]; // freeUdf)
    }

    public int createFunctionAggregate(
            int dbPtr, int namePtr, int nArgs, int flags, int userData, boolean isWindow) {
        return (int)
                createFunctionAggregate
                        .apply(
                                dbPtr,
                                namePtr,
                                nArgs,
                                SQLITE_UTF16 | flags,
                                userData,
                                xStepPtr,
                                xFinalPtr,
                                (isWindow) ? xValuePtr : 0,
                                (isWindow) ? xInversePtr : 0,
                                xDestroyPtr)[0]; // freeUdf)
    }

    public int createNullFunction(int dbPtr, int namePtr) {
        return (int)
                createFunction.apply(dbPtr, namePtr, 0, SQLITE_UTF16, 0, 0, 0, 0, 0)[0]; // freeUdf)
    }

    public int userData(int ctx) {
        return (int) userData.apply(ctx)[0];
    }

    public void resultText(int context, int bytesPtr, int bytesLength) {
        resultText.apply(context, bytesPtr, bytesLength, SQLITE_TRANSIENT);
    }

    public void resultInt(int context, int value) {
        resultInt.apply(context, value);
    }

    public void resultLong(int context, long value) {
        resultLong.apply(context, value);
    }

    public void resultDouble(int context, double value) {
        resultDouble.apply(context, Value.doubleToLong(value));
    }

    public void resultBlob(int context, int bytesPtr, int bytesLength) {
        resultBlob.apply(context, bytesPtr, bytesLength, SQLITE_TRANSIENT);
    }

    public double valueDouble(int valuePtr) {
        return Value.longToDouble(valueDouble.apply(valuePtr)[0]);
    }

    public int valueText(int valuePtr) {
        return (int) valueText.apply(valuePtr)[0];
    }

    public int valueInt(int valuePtr) {
        return (int) valueInt.apply(valuePtr)[0];
    }

    public long valueLong(int valuePtr) {
        return valueLong.apply(valuePtr)[0];
    }

    public int valueBlob(int valuePtr) {
        return (int) valueBlob.apply(valuePtr)[0];
    }

    public int valueBytes(int valuePtr) {
        return (int) valueBytes.apply(valuePtr)[0];
    }

    public void progressHandler(int dbPtr, int vmCalls, int userData) {
        progressHandler.apply(dbPtr, vmCalls, xProgressPtr, userData);
    }

    public void busyHandler(int dbPtr, int userData) {
        busyHandler.apply(dbPtr, xBusyPtr, userData);
    }
}
