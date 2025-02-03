package io.roastedroot.sqlite4j.core.wasm;

public class DummyWasmDBImports implements WasmDBImports {
    @Override
    public int xProgress(int userData) {
        return 0;
    }

    @Override
    public int xBusy(int userData, int nbPrevInvok) {
        return 0;
    }

    @Override
    public void xDestroy(int funIdx) {}

    @Override
    public void xFinal(int ctx) {}

    @Override
    public void xValue(int ctx) {}

    @Override
    public void xFunc(int ctx, int argN, int value) {}

    @Override
    public void xStep(int ctx, int argN, int value) {}

    @Override
    public void xInverse(int ctx, int argN, int value) {}

    @Override
    public int xCompare(int ctx, int len1, int str1Ptr, int len2, int str2Ptr) {
        return 0;
    }

    @Override
    public void xDestroyCollation(int funIdx) {}

    @Override
    public void xUpdate(int userData, int updateType, int dbNamePtr, int tablePtr, long rowId) {}

    @Override
    public int xCommit(int userData) {
        return 0;
    }

    @Override
    public void xRollback(int userData) {}
}
