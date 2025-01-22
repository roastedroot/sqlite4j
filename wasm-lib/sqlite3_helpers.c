// Additional functions to be referenced/used by WASM

#include "sqlite3.h"

#define __IMPORT(name) \
    __attribute__((__import_module__("env"), __import_name__(#name)))

extern void xFunc(sqlite3_context *context, int args, sqlite3_value** value)
__attribute__((
    __import_module__("env"),
    __import_name__("xFunc"),
));

extern void* xFuncPtr() {
    return &xFunc;
}

extern void xDestroy(void *userData)
__attribute__((
    __import_module__("env"),
    __import_name__("xDestroy"),
));

extern void* xDestroyPtr() {
    return &xDestroy;
}
