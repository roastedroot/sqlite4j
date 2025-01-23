package com.github.andreaTP.sqlite.wasm.wasm;

import com.github.andreaTP.sqlite.wasm.ProgressHandler;
import java.util.ArrayDeque;

// TODO: verify
// Instead of it being static we can have a specific UDFStore per Database
public class ProgressHandlerStore {
    // We have only one progress handler available per connection
    private static int MIN_CAPACITY = 1;
    private static int count;
    private static ArrayDeque<Integer> emptySlots = new ArrayDeque<>();
    private static ProgressHandler[] store = new ProgressHandler[MIN_CAPACITY];

    private static void increaseCapacity() {
        final int newCapacity = store.length << 1;

        final ProgressHandler[] array = new ProgressHandler[newCapacity];
        System.arraycopy(store, 0, array, 0, store.length);

        store = array;
    }

    public static int registerProgressHandler(ProgressHandler f) {
        if (emptySlots.isEmpty()) {
            store[count] = f;
            count++;

            if (count == store.length) {
                increaseCapacity();
            }

            // TODO: we should probably never return 0 even for the UDFStore
            // to be able to distinguish NULL
            // Hack to let the tests run almost unmodified
            return (count - 1);
        } else {
            int emptySlot = emptySlots.pop();

            store[emptySlot] = f;
            return emptySlot;
        }
    }

    public static void free(int idx) {
        store[idx] = null;
        emptySlots.push(idx);
    }

    public static ProgressHandler get(int idx) {
        return store[idx];
    }
}
