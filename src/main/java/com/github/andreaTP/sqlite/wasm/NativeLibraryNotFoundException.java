package com.github.andreaTP.sqlite.wasm;

public class NativeLibraryNotFoundException extends Exception {
    public NativeLibraryNotFoundException(String message) {
        super(message);
    }
}
