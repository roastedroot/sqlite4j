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

extern void xStep(sqlite3_context *context, int args, sqlite3_value** value)
__attribute__((
    __import_module__("env"),
    __import_name__("xStep"),
));

extern void* xStepPtr() {
    return &xStep;
}

extern void xFinal(sqlite3_context *context)
__attribute__((
    __import_module__("env"),
    __import_name__("xFinal"),
));

extern void* xFinalPtr() {
    return &xFinal;
}

extern void xValue(sqlite3_context *context)
__attribute__((
    __import_module__("env"),
    __import_name__("xValue"),
));

extern void* xValuePtr() {
    return &xValue;
}

extern void xInverse(sqlite3_context *context, int args, sqlite3_value** value)
__attribute__((
    __import_module__("env"),
    __import_name__("xInverse"),
));

extern void* xInversePtr() {
    return &xInverse;
}

extern void xDestroy(void *userData)
__attribute__((
    __import_module__("env"),
    __import_name__("xDestroy"),
));

extern void* xDestroyPtr() {
    return &xDestroy;
}

extern int xProgress(void *userData)
__attribute__((
    __import_module__("env"),
    __import_name__("xProgress"),
));

extern void* xProgressPtr() {
    return &xProgress;
}

extern int xBusy(void *userData, int nbPrevInvok)
__attribute__((
    __import_module__("env"),
    __import_name__("xBusy"),
));

extern void* xBusyPtr() {
    return &xBusy;
}
