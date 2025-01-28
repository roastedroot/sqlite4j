package com.github.andreaTP.sqlite.wasm.wasm;

import com.github.andreaTP.sqlite.wasm.Collation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollationStore {
    // we need to avoid returning 0 to disambiguate with NULL
    private static int OFFSET = 1;
    private static int MIN_CAPACITY = 8;
    private static int count;
    private static ArrayDeque<Integer> emptySlots = new ArrayDeque<>();
    private static Collation[] store = new Collation[MIN_CAPACITY];
    private static Map<String, List<Integer>> names = new HashMap<>();

    public CollationStore() {}

    private static void increaseCapacity() {
        final int newCapacity = store.length << 1;

        final Collation[] array = new Collation[newCapacity];
        System.arraycopy(store, 0, array, 0, store.length);

        store = array;
    }

    public int registerCollation(String name, Collation f) {
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
        if (names.containsKey(name)) {
            List<Integer> newValue = names.get(name);
            newValue.add(result);
            names.put(name, newValue);
        } else {
            List<Integer> value = new ArrayList<>();
            value.add(result);
            names.put(name, value);
        }
        return result + OFFSET;
    }

    public void free(String name) {
        if (names.containsKey(name)) {
            for (int v : names.get(name)) {
                free(v + OFFSET);
            }
            names.remove(name);
        }
    }

    public void free(int idx) {
        idx = idx - OFFSET;
        store[idx] = null;
        emptySlots.push(idx);
    }

    public Collation get(int idx) {
        idx = idx - OFFSET;
        return store[idx];
    }
}
