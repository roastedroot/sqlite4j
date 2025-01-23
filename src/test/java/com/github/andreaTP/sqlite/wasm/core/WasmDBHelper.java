package com.github.andreaTP.sqlite.wasm.core;

/** This is a helper class for exposing package local functions of NativeDB to unit tests */
public class WasmDBHelper {
    /**
     * Get the native pointer of the progress handler
     *
     * @param nativeDB the native db object
     * @return the pointer of the progress handler
     */
    public static long getProgressHandler(DB nativeDB) {
        return ((WasmDB) nativeDB).getProgressHandler();
    }

    /**
     * Get the native pointer of the busy handler
     *
     * @param nativeDB the native db object
     * @return the pointer of the busy handler
     */
    public static long getBusyHandler(DB nativeDB) {
        // return ((WasmDB) nativeDB).getBusyHandler();
        return 0;
    }

    /**
     * Get the native pointer of the commit listener
     *
     * @param nativeDB the native db object
     * @return the pointer of the commit listener
     */
    public static long getCommitListener(DB nativeDB) {
        // return ((WasmDB) nativeDB).getCommitListener();
        return 0;
    }

    /**
     * Get the native pointer of the update listener
     *
     * @param nativeDB the native db object
     * @return the pointer of the update listener
     */
    public static long getUpdateListener(DB nativeDB) {
        // return ((WasmDB) nativeDB).getUpdateListener();
        return 0;
    }
}
