package io.roastedroot.sqlite4j.core.wasm;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;
import java.util.List;

public interface WasmDBImports {

    int xProgress(int userData);

    int xBusy(int userData, int nbPrevInvok);

    void xDestroy(int funIdx);

    void xFinal(int ctx);

    void xValue(int ctx);

    void xFunc(int ctx, int argN, int value);

    void xStep(int ctx, int argN, int value);

    void xInverse(int ctx, int argN, int value);

    int xCompare(int ctx, int len1, int str1Ptr, int len2, int str2Ptr);

    void xDestroyCollation(int funIdx);

    void xUpdate(int userData, int updateType, int dbNamePtr, int tablePtr, long rowId);

    int xCommit(int userData);

    void xRollback(int userData);

    default HostFunction[] toHostFunctions() {
        return new HostFunction[] {
            new HostFunction(
                    "env",
                    "xFunc",
                    List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                    List.of(),
                    (inst, args) -> {
                        xFunc((int) args[0], (int) args[1], (int) args[2]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xStep",
                    List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                    List.of(),
                    (inst, args) -> {
                        xStep((int) args[0], (int) args[1], (int) args[2]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xFinal",
                    List.of(ValueType.I32),
                    List.of(),
                    (inst, args) -> {
                        xFinal((int) args[0]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xValue",
                    List.of(ValueType.I32),
                    List.of(),
                    (inst, args) -> {
                        xValue((int) args[0]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xInverse",
                    List.of(ValueType.I32, ValueType.I32, ValueType.I32),
                    List.of(),
                    (inst, args) -> {
                        xInverse((int) args[0], (int) args[1], (int) args[2]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xDestroy",
                    List.of(ValueType.I32),
                    List.of(),
                    (inst, args) -> {
                        xDestroy((int) args[0]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xProgress",
                    List.of(ValueType.I32),
                    List.of(ValueType.I32),
                    (inst, args) -> new long[] {xProgress((int) args[0])}),
            new HostFunction(
                    "env",
                    "xBusy",
                    List.of(ValueType.I32, ValueType.I32),
                    List.of(ValueType.I32),
                    (inst, args) -> {
                        xBusy((int) args[0], (int) args[1]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xCompare",
                    List.of(
                            ValueType.I32,
                            ValueType.I32,
                            ValueType.I32,
                            ValueType.I32,
                            ValueType.I32),
                    List.of(ValueType.I32),
                    (inst, args) ->
                            new long[] {
                                xCompare(
                                        (int) args[0],
                                        (int) args[1],
                                        (int) args[2],
                                        (int) args[3],
                                        (int) args[4])
                            }),
            new HostFunction(
                    "env",
                    "xDestroyCollation",
                    List.of(ValueType.I32),
                    List.of(),
                    (inst, args) -> {
                        xDestroyCollation((int) args[0]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xUpdate",
                    List.of(
                            ValueType.I32,
                            ValueType.I32,
                            ValueType.I32,
                            ValueType.I32,
                            ValueType.I64),
                    List.of(),
                    (inst, args) -> {
                        xUpdate(
                                (int) args[0],
                                (int) args[1],
                                (int) args[2],
                                (int) args[3],
                                args[4]);
                        return null;
                    }),
            new HostFunction(
                    "env",
                    "xCommit",
                    List.of(ValueType.I32),
                    List.of(ValueType.I32),
                    (inst, args) -> new long[] {xCommit((int) args[0])}),
            new HostFunction(
                    "env",
                    "xRollback",
                    List.of(ValueType.I32),
                    List.of(),
                    (inst, args) -> {
                        xRollback((int) args[0]);
                        return null;
                    })
        };
    }
}
