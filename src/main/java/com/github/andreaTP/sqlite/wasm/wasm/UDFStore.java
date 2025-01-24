package com.github.andreaTP.sqlite.wasm.wasm;

import com.github.andreaTP.sqlite.wasm.Function;
import java.util.ArrayDeque;

public class UDFStore {
    // we need to avoid returning 0 to disambiguate with NULL
    private int OFFSET = 1;
    private int MIN_CAPACITY = 8;
    private int count;
    private ArrayDeque<Integer> emptySlots = new ArrayDeque<>();
    private Function[] store = new Function[MIN_CAPACITY];

    private void increaseCapacity() {
        final int newCapacity = store.length << 1;

        final Function[] array = new Function[newCapacity];
        System.arraycopy(store, 0, array, 0, store.length);

        store = array;
    }

    public int registerFunction(Function f) {
        int result;
        if (emptySlots.isEmpty()) {
            store[count] = f;
            count++;

            if (count == store.length) {
                increaseCapacity();
            }
            result = (count - 1);
        } else {
            int emptySlot = emptySlots.pop();
            // just a sanity check
            assert (store[emptySlot] == null);

            store[emptySlot] = f;
            result = emptySlot;
        }
        return result + OFFSET;
    }

    public void free(int idx) {
        idx = idx - OFFSET;
        store[idx] = null;
        emptySlots.push(idx);
    }

    public Function get(int idx) {
        idx = idx - OFFSET;
        return store[idx];
    }
}
