package com.github.andreaTP.sqlite.wasm.wasm;

import static com.github.andreaTP.sqlite.wasm.core.Codes.SQLITE_NULL;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.types.Value;

import java.nio.charset.StandardCharsets;

// Manually writing it to avoid passing through the Map lookup on every invocation
public class WasmDBExports {

    private static final int SQLITE_TRANSIENT = -1; // https://www.sqlite.org/c3ref/c_static.html

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
    private final ExportFunction bindDouble;
    private final ExportFunction bindNull;
    private final ExportFunction bindText;
    private final ExportFunction limit;
    private final ExportFunction errmsg;
    private final ExportFunction extendedErrcode;

    public WasmDBExports(Instance instance) {
        this.instance = instance;
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
        this.bindDouble = instance.exports().function("sqlite3_bind_double");
        this.bindNull = instance.exports().function("sqlite3_bind_null");
        this.bindText = instance.exports().function("sqlite3_bind_text");
        this.limit = instance.exports().function("sqlite3_limit");
        this.errmsg = instance.exports().function("sqlite3_errmsg");
        this.extendedErrcode = instance.exports().function("sqlite3_extended_errcode");
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
    public int openV2(int filenamePtr, /* OUT */ int dbPtrPtr, int flags, int zVfs) {
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

    public int bindDouble(int stmtPtr, int pos, double v) {
        return (int) bindDouble.apply(stmtPtr, pos, Value.doubleToLong(v))[0];
    }

    public int bindNull(int stmtPtr, int pos) {
        return (int) bindNull.apply(stmtPtr, pos)[0];
    }

    public int bindText(int stmtPtr, int pos, int vPtr, int vLength) {
        return (int) bindText.apply(stmtPtr, pos, vPtr, vLength, SQLITE_TRANSIENT)[0];
    }

    public int errmsg(int dbPtr) {
        return (int) errmsg.apply(dbPtr)[0];
    }
}
