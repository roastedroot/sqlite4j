package com.github.andreaTP.sqlite.wasm.wasm;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;

import java.nio.charset.StandardCharsets;

// Manually writing it to avoid passing through the Map lookup on every invocation
public class WasmDBExports {

    private final Instance instance;
    private final ExportFunction realloc;
    private final ExportFunction openV2;
    private final ExportFunction prepareV2;

    public WasmDBExports(Instance instance) {
        this.instance = instance;
        this.realloc = instance.exports().function("realloc");
        this.openV2 = instance.exports().function("sqlite3_open_v2");
        this.prepareV2 = instance.exports().function("sqlite3_prepare_v2");
    }

    public int malloc(int size) {
        return (int) realloc.apply(0, size)[0];
    }

    public int free(int ptr) {
        return (int) realloc.apply(ptr, 0)[0];
    }

    public int allocCString(String str) {
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[strBytes.length + 1];
        System.arraycopy(strBytes, 0, bytes, 0, strBytes.length);
        bytes[strBytes.length - 1] = 0;

        int strBytesPtr = malloc(bytes.length);
        instance.memory().write(strBytesPtr, bytes);
        return strBytesPtr;
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
    public int prepareV2(int dbPtr, int zSql, int nByte, /* OUT */ int stmtPtrPtr, /* OUT */ int pzTail) {
        return (int) openV2.apply(dbPtr, zSql, nByte, stmtPtrPtr, pzTail)[0];
    }

}
