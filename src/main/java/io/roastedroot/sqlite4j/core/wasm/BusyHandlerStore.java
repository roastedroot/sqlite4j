package io.roastedroot.sqlite4j.core.wasm;

import io.roastedroot.sqlite4j.BusyHandler;
import java.util.HashMap;
import java.util.Map;

// TODO: refactor with ProgressHandler?
public class BusyHandlerStore {
    private static Map<Integer, BusyHandler> store = new HashMap<>();

    public static int registerBusyHandler(int db, BusyHandler f) {
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

    public static BusyHandler get(int idx) {
        return store.get(idx);
    }

    public static boolean isEmpty(int idx) {
        return !store.containsKey(idx);
    }
}
