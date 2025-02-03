package io.roastedroot.sqlite4j;

public class NativeLibraryNotFoundException extends Exception {
    public NativeLibraryNotFoundException(String message) {
        super(message);
    }
}
