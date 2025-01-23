package com.github.andreaTP.sqlite.wasm.wasm;

import com.github.andreaTP.sqlite.wasm.Function;
import java.util.ArrayDeque;

// TODO: verify
// Instead of it being static we can have a specific UDFStore per Database
public class UDFStore {

    private static int MIN_CAPACITY = 8;
    private static int count;
    private static ArrayDeque<Integer> emptySlots = new ArrayDeque<>();
    private static Function[] store = new Function[MIN_CAPACITY];

    private static void increaseCapacity() {
        final int newCapacity = store.length << 1;

        final Function[] array = new Function[newCapacity];
        System.arraycopy(store, 0, array, 0, store.length);

        store = array;
    }

    public static int registerFunction(Function f) {
        if (emptySlots.isEmpty()) {
            store[count] = f;
            count++;

            if (count == store.length) {
                increaseCapacity();
            }
            return (count - 1);
        } else {
            int emptySlot = emptySlots.pop();
            // just a sanity check
            assert (store[emptySlot] == null);

            store[emptySlot] = f;
            return emptySlot;
        }
    }

    public static void free(int idx) {
        store[idx] = null;
        emptySlots.push(idx);
    }

    public static Function get(int idx) {
        return store[idx];
    }
}
