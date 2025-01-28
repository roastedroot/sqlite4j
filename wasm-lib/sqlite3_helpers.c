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

extern int xCompare(void* context, int len1, const void* str1, int len2, const void* str2)
__attribute__((
    __import_module__("env"),
    __import_name__("xCompare"),
));

extern void* xComparePtr() {
    return &xCompare;
}

extern void xDestroyCollation(void *userData)
__attribute__((
    __import_module__("env"),
    __import_name__("xDestroyCollation"),
));

extern void* xDestroyCollationPtr() {
    return &xDestroyCollation;
}

extern void xUpdate(void *userData, int type, char const *dbPtr, char const *tablePtr, sqlite3_int64 rowId)
__attribute__((
    __import_module__("env"),
    __import_name__("xUpdate"),
));

extern void* xUpdatePtr() {
    return &xUpdate;
}

extern int xCommit(void *userData)
__attribute__((
    __import_module__("env"),
    __import_name__("xCommit"),
));

extern void* xCommitPtr() {
    return &xCommit;
}

extern void xRollback(void *userData)
__attribute__((
    __import_module__("env"),
    __import_name__("xRollback"),
));

extern void* xRollbackPtr() {
    return &xRollback;
}
