package com.github.andreaTP.sqlite.wasm.core.wasm;

import com.github.andreaTP.sqlite.wasm.ProgressHandler;
import java.util.HashMap;
import java.util.Map;

public class ProgressHandlerStore {
    private static Map<Integer, ProgressHandler> store = new HashMap<>();

    public static int registerProgressHandler(int db, ProgressHandler f) {
        // registering null is the same as freeing
        if (f == null) {
            free(db);
        } else {
            store.put(db, f);
        }
        return db;
    }

    public static void free(int idx) {
        store.remove(idx);
    }

    public static ProgressHandler get(int idx) {
        return store.get(idx);
    }

    public static boolean isEmpty(int idx) {
        return !store.containsKey(idx);
    }
}
