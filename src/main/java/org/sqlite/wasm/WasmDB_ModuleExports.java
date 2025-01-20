package org.sqlite.wasm;

import com.dylibso.chicory.runtime.GlobalInstance;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.runtime.TableInstance;
import com.dylibso.chicory.wasm.types.Value;

// import javax.annotation.processing.Generated;

// TODO: roll out a final version of this class with memoized accessors
// Exporting everything for now to avoid re-generating this often, trim it at the end
// @Generated("com.dylibso.chicory.experimental.hostmodule.processor.WasmModuleProcessor")
public interface WasmDB_ModuleExports {

    Instance instance();

    default Memory memory() {
        return instance().exports().memory("memory");
    }

    default void WasmCallCtors() {
        instance().exports().function("__wasm_call_ctors").apply();
        return;
    }

    default void Initialize() {
        instance().exports().function("_initialize").apply();
        return;
    }

    default GlobalInstance MemoryBase() {
        return instance().exports().global("__memory_base");
    }

    default TableInstance IndirectFunctionTable() {
        return instance().exports().table("__indirect_function_table");
    }

    default int sqlite3Status64(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_status64")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3Status(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_status")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3DbStatus(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_db_status")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default long sqlite3Msize(int arg0) {
        long result = instance().exports().function("sqlite3_msize").apply((long) arg0)[0];
        return result;
    }

    default int sqlite3VfsFind(int arg0) {
        long result = instance().exports().function("sqlite3_vfs_find").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Initialize() {
        long result = instance().exports().function("sqlite3_initialize").apply()[0];
        return (int) result;
    }

    default int strcmp(int arg0, int arg1) {
        long result = instance().exports().function("strcmp").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int memset(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("memset")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3Malloc(int arg0) {
        long result = instance().exports().function("sqlite3_malloc").apply((long) arg0)[0];
        return (int) result;
    }

    default void sqlite3Free(int arg0) {
        instance().exports().function("sqlite3_free").apply((long) arg0);
        return;
    }

    default int sqlite3OsInit() {
        long result = instance().exports().function("sqlite3_os_init").apply()[0];
        return (int) result;
    }

    default int sqlite3VfsRegister(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vfs_register")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3VfsUnregister(int arg0) {
        long result = instance().exports().function("sqlite3_vfs_unregister").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ReleaseMemory(int arg0) {
        long result = instance().exports().function("sqlite3_release_memory").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3MemoryAlarm(int arg0, int arg1, long arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_memory_alarm")
                        .apply((long) arg0, (long) arg1, arg2)[0];
        return (int) result;
    }

    default long sqlite3SoftHeapLimit64(long arg0) {
        long result = instance().exports().function("sqlite3_soft_heap_limit64").apply(arg0)[0];
        return result;
    }

    default long sqlite3MemoryUsed() {
        long result = instance().exports().function("sqlite3_memory_used").apply()[0];
        return result;
    }

    default void sqlite3SoftHeapLimit(int arg0) {
        instance().exports().function("sqlite3_soft_heap_limit").apply((long) arg0);
        return;
    }

    default long sqlite3HardHeapLimit64(long arg0) {
        long result = instance().exports().function("sqlite3_hard_heap_limit64").apply(arg0)[0];
        return result;
    }

    default long sqlite3MemoryHighwater(int arg0) {
        long result =
                instance().exports().function("sqlite3_memory_highwater").apply((long) arg0)[0];
        return result;
    }

    default int sqlite3Malloc64(long arg0) {
        long result = instance().exports().function("sqlite3_malloc64").apply(arg0)[0];
        return (int) result;
    }

    default int sqlite3Realloc(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_realloc").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Realloc64(int arg0, long arg1) {
        long result =
                instance().exports().function("sqlite3_realloc64").apply((long) arg0, arg1)[0];
        return (int) result;
    }

    default void sqlite3StrVappendf(int arg0, int arg1, int arg2) {
        instance()
                .exports()
                .function("sqlite3_str_vappendf")
                .apply((long) arg0, (long) arg1, (long) arg2);
        return;
    }

    default void sqlite3StrAppend(int arg0, int arg1, int arg2) {
        instance()
                .exports()
                .function("sqlite3_str_append")
                .apply((long) arg0, (long) arg1, (long) arg2);
        return;
    }

    default void sqlite3StrAppendchar(int arg0, int arg1, int arg2) {
        instance()
                .exports()
                .function("sqlite3_str_appendchar")
                .apply((long) arg0, (long) arg1, (long) arg2);
        return;
    }

    default int strlen(int arg0) {
        long result = instance().exports().function("strlen").apply((long) arg0)[0];
        return (int) result;
    }

    default void sqlite3StrAppendall(int arg0, int arg1) {
        instance().exports().function("sqlite3_str_appendall").apply((long) arg0, (long) arg1);
        return;
    }

    default void sqlite3StrAppendf(int arg0, int arg1, int arg2) {
        instance()
                .exports()
                .function("sqlite3_str_appendf")
                .apply((long) arg0, (long) arg1, (long) arg2);
        return;
    }

    default int memcpy(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("memcpy")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3ValueText(int arg0) {
        long result = instance().exports().function("sqlite3_value_text").apply((long) arg0)[0];
        return (int) result;
    }

    default void sqlite3StrReset(int arg0) {
        instance().exports().function("sqlite3_str_reset").apply((long) arg0);
        return;
    }

    default int sqlite3StrFinish(int arg0) {
        long result = instance().exports().function("sqlite3_str_finish").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3StrErrcode(int arg0) {
        long result = instance().exports().function("sqlite3_str_errcode").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3StrLength(int arg0) {
        long result = instance().exports().function("sqlite3_str_length").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3StrValue(int arg0) {
        long result = instance().exports().function("sqlite3_str_value").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3StrNew(int arg0) {
        long result = instance().exports().function("sqlite3_str_new").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Vmprintf(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vmprintf")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Mprintf(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_mprintf").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Vsnprintf(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vsnprintf")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3Snprintf(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_snprintf")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default void sqlite3Log(int arg0, int arg1, int arg2) {
        instance().exports().function("sqlite3_log").apply((long) arg0, (long) arg1, (long) arg2);
        return;
    }

    default void sqlite3Randomness(int arg0, int arg1) {
        instance().exports().function("sqlite3_randomness").apply((long) arg0, (long) arg1);
        return;
    }

    default int sqlite3Stricmp(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_stricmp").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Strnicmp(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_strnicmp")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int getenv(int arg0) {
        long result = instance().exports().function("getenv").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3UriParameter(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_uri_parameter")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default GlobalInstance errno() {
        return instance().exports().global("errno");
    }

    default int sqlite3UriBoolean(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_uri_boolean")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int memcmp(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("memcmp")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int fsync(int arg0) {
        long result = instance().exports().function("fsync").apply((long) arg0)[0];
        return (int) result;
    }

    default long time(int arg0) {
        long result = instance().exports().function("time").apply((long) arg0)[0];
        return result;
    }

    default int nanosleep(int arg0, int arg1) {
        long result = instance().exports().function("nanosleep").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int gettimeofday(int arg0, int arg1) {
        long result =
                instance().exports().function("gettimeofday").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3OsEnd() {
        long result = instance().exports().function("sqlite3_os_end").apply()[0];
        return (int) result;
    }

    default int sqlite3Serialize(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_serialize")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3PrepareV2(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_prepare_v2")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3Step(int arg0) {
        long result = instance().exports().function("sqlite3_step").apply((long) arg0)[0];
        return (int) result;
    }

    default long sqlite3ColumnInt64(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_int64")
                        .apply((long) arg0, (long) arg1)[0];
        return result;
    }

    default int sqlite3Reset(int arg0) {
        long result = instance().exports().function("sqlite3_reset").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Exec(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_exec")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3ColumnInt(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_int")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Finalize(int arg0) {
        long result = instance().exports().function("sqlite3_finalize").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3FileControl(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_file_control")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3ColumnName(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_name")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ColumnText(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_text")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ColumnType(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_type")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Errmsg(int arg0) {
        long result = instance().exports().function("sqlite3_errmsg").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Deserialize(int arg0, int arg1, int arg2, long arg3, long arg4, int arg5) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_deserialize")
                        .apply((long) arg0, (long) arg1, (long) arg2, arg3, arg4, (long) arg5)[0];
        return (int) result;
    }

    default int sqlite3DatabaseFileObject(int arg0) {
        long result =
                instance().exports().function("sqlite3_database_file_object").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3EnableSharedCache(int arg0) {
        long result =
                instance().exports().function("sqlite3_enable_shared_cache").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3BackupInit(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_backup_init")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3BackupStep(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_backup_step")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3BackupFinish(int arg0) {
        long result = instance().exports().function("sqlite3_backup_finish").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3BackupRemaining(int arg0) {
        long result =
                instance().exports().function("sqlite3_backup_remaining").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3BackupPagecount(int arg0) {
        long result =
                instance().exports().function("sqlite3_backup_pagecount").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Expired(int arg0) {
        long result = instance().exports().function("sqlite3_expired").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ClearBindings(int arg0) {
        long result = instance().exports().function("sqlite3_clear_bindings").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueBlob(int arg0) {
        long result = instance().exports().function("sqlite3_value_blob").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueBytes(int arg0) {
        long result = instance().exports().function("sqlite3_value_bytes").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueBytes16(int arg0) {
        long result = instance().exports().function("sqlite3_value_bytes16").apply((long) arg0)[0];
        return (int) result;
    }

    default double sqlite3ValueDouble(int arg0) {
        long result = instance().exports().function("sqlite3_value_double").apply((long) arg0)[0];
        return Value.longToDouble(result);
    }

    default int sqlite3ValueInt(int arg0) {
        long result = instance().exports().function("sqlite3_value_int").apply((long) arg0)[0];
        return (int) result;
    }

    default long sqlite3ValueInt64(int arg0) {
        long result = instance().exports().function("sqlite3_value_int64").apply((long) arg0)[0];
        return result;
    }

    default int sqlite3ValueSubtype(int arg0) {
        long result = instance().exports().function("sqlite3_value_subtype").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValuePointer(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_value_pointer")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ValueText16(int arg0) {
        long result = instance().exports().function("sqlite3_value_text16").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueText16be(int arg0) {
        long result = instance().exports().function("sqlite3_value_text16be").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueText16le(int arg0) {
        long result = instance().exports().function("sqlite3_value_text16le").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueType(int arg0) {
        long result = instance().exports().function("sqlite3_value_type").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueEncoding(int arg0) {
        long result = instance().exports().function("sqlite3_value_encoding").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueNochange(int arg0) {
        long result = instance().exports().function("sqlite3_value_nochange").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueFrombind(int arg0) {
        long result = instance().exports().function("sqlite3_value_frombind").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueDup(int arg0) {
        long result = instance().exports().function("sqlite3_value_dup").apply((long) arg0)[0];
        return (int) result;
    }

    default void sqlite3ValueFree(int arg0) {
        instance().exports().function("sqlite3_value_free").apply((long) arg0);
        return;
    }

    default void sqlite3ResultBlob(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_result_blob")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default void sqlite3ResultErrorNomem(int arg0) {
        instance().exports().function("sqlite3_result_error_nomem").apply((long) arg0);
        return;
    }

    default void sqlite3ResultErrorToobig(int arg0) {
        instance().exports().function("sqlite3_result_error_toobig").apply((long) arg0);
        return;
    }

    default void sqlite3ResultBlob64(int arg0, int arg1, long arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_result_blob64")
                .apply((long) arg0, (long) arg1, arg2, (long) arg3);
        return;
    }

    default void sqlite3ResultDouble(int arg0, double arg1) {
        instance()
                .exports()
                .function("sqlite3_result_double")
                .apply((long) arg0, Value.doubleToLong(arg1));
        return;
    }

    default void sqlite3ResultError(int arg0, int arg1, int arg2) {
        instance()
                .exports()
                .function("sqlite3_result_error")
                .apply((long) arg0, (long) arg1, (long) arg2);
        return;
    }

    default int memmove(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("memmove")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default void sqlite3ResultError16(int arg0, int arg1, int arg2) {
        instance()
                .exports()
                .function("sqlite3_result_error16")
                .apply((long) arg0, (long) arg1, (long) arg2);
        return;
    }

    default void sqlite3ResultInt(int arg0, int arg1) {
        instance().exports().function("sqlite3_result_int").apply((long) arg0, (long) arg1);
        return;
    }

    default void sqlite3ResultInt64(int arg0, long arg1) {
        instance().exports().function("sqlite3_result_int64").apply((long) arg0, arg1);
        return;
    }

    default void sqlite3ResultNull(int arg0) {
        instance().exports().function("sqlite3_result_null").apply((long) arg0);
        return;
    }

    default void sqlite3ResultPointer(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_result_pointer")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default void sqlite3ResultSubtype(int arg0, int arg1) {
        instance().exports().function("sqlite3_result_subtype").apply((long) arg0, (long) arg1);
        return;
    }

    default void sqlite3ResultText(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_result_text")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default void sqlite3ResultText64(int arg0, int arg1, long arg2, int arg3, int arg4) {
        instance()
                .exports()
                .function("sqlite3_result_text64")
                .apply((long) arg0, (long) arg1, arg2, (long) arg3, (long) arg4);
        return;
    }

    default void sqlite3ResultText16(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_result_text16")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default void sqlite3ResultText16be(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_result_text16be")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default void sqlite3ResultText16le(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_result_text16le")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default void sqlite3ResultValue(int arg0, int arg1) {
        instance().exports().function("sqlite3_result_value").apply((long) arg0, (long) arg1);
        return;
    }

    default void sqlite3ResultZeroblob(int arg0, int arg1) {
        instance().exports().function("sqlite3_result_zeroblob").apply((long) arg0, (long) arg1);
        return;
    }

    default int sqlite3ResultZeroblob64(int arg0, long arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_result_zeroblob64")
                        .apply((long) arg0, arg1)[0];
        return (int) result;
    }

    default void sqlite3ResultErrorCode(int arg0, int arg1) {
        instance().exports().function("sqlite3_result_error_code").apply((long) arg0, (long) arg1);
        return;
    }

    default int sqlite3UserData(int arg0) {
        long result = instance().exports().function("sqlite3_user_data").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ContextDbHandle(int arg0) {
        long result =
                instance().exports().function("sqlite3_context_db_handle").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3VtabNochange(int arg0) {
        long result = instance().exports().function("sqlite3_vtab_nochange").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3VtabInFirst(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vtab_in_first")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3VtabInNext(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vtab_in_next")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3AggregateContext(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_aggregate_context")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3GetAuxdata(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_get_auxdata")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default void sqlite3SetAuxdata(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_set_auxdata")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default int sqlite3AggregateCount(int arg0) {
        long result =
                instance().exports().function("sqlite3_aggregate_count").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ColumnCount(int arg0) {
        long result = instance().exports().function("sqlite3_column_count").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3DataCount(int arg0) {
        long result = instance().exports().function("sqlite3_data_count").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ColumnBlob(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_blob")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ColumnBytes(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_bytes")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ColumnBytes16(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_bytes16")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default double sqlite3ColumnDouble(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_double")
                        .apply((long) arg0, (long) arg1)[0];
        return Value.longToDouble(result);
    }

    default int sqlite3ColumnValue(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_value")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ColumnText16(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_text16")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ColumnName16(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_name16")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ColumnDecltype(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_decltype")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3ColumnDecltype16(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_column_decltype16")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3BindBlob(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_blob")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3BindBlob64(int arg0, int arg1, int arg2, long arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_blob64")
                        .apply((long) arg0, (long) arg1, (long) arg2, arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3BindDouble(int arg0, int arg1, double arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_double")
                        .apply((long) arg0, (long) arg1, Value.doubleToLong(arg2))[0];
        return (int) result;
    }

    default int sqlite3BindInt(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_int")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3BindInt64(int arg0, int arg1, long arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_int64")
                        .apply((long) arg0, (long) arg1, arg2)[0];
        return (int) result;
    }

    default int sqlite3BindNull(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_null")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3BindPointer(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_pointer")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3BindText(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_text")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3BindText64(int arg0, int arg1, int arg2, long arg3, int arg4, int arg5) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_text64")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                arg3,
                                (long) arg4,
                                (long) arg5)[0];
        return (int) result;
    }

    default int sqlite3BindText16(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_text16")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3BindValue(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_value")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3BindZeroblob(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_zeroblob")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3BindZeroblob64(int arg0, int arg1, long arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_zeroblob64")
                        .apply((long) arg0, (long) arg1, arg2)[0];
        return (int) result;
    }

    default int sqlite3BindParameterCount(int arg0) {
        long result =
                instance().exports().function("sqlite3_bind_parameter_count").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3BindParameterName(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_parameter_name")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3BindParameterIndex(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_bind_parameter_index")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3TransferBindings(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_transfer_bindings")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3DbHandle(int arg0) {
        long result = instance().exports().function("sqlite3_db_handle").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3StmtReadonly(int arg0) {
        long result = instance().exports().function("sqlite3_stmt_readonly").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3StmtIsexplain(int arg0) {
        long result = instance().exports().function("sqlite3_stmt_isexplain").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3StmtExplain(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_stmt_explain")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3StmtBusy(int arg0) {
        long result = instance().exports().function("sqlite3_stmt_busy").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3NextStmt(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_next_stmt")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3StmtStatus(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_stmt_status")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3Sql(int arg0) {
        long result = instance().exports().function("sqlite3_sql").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ExpandedSql(int arg0) {
        long result = instance().exports().function("sqlite3_expanded_sql").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ValueNumericType(int arg0) {
        long result =
                instance().exports().function("sqlite3_value_numeric_type").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3BlobOpen(
            int arg0, int arg1, int arg2, int arg3, long arg4, int arg5, int arg6) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_blob_open")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                arg4,
                                (long) arg5,
                                (long) arg6)[0];
        return (int) result;
    }

    default int sqlite3BlobClose(int arg0) {
        long result = instance().exports().function("sqlite3_blob_close").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3BlobRead(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_blob_read")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3BlobWrite(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_blob_write")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3BlobBytes(int arg0) {
        long result = instance().exports().function("sqlite3_blob_bytes").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3BlobReopen(int arg0, long arg1) {
        long result =
                instance().exports().function("sqlite3_blob_reopen").apply((long) arg0, arg1)[0];
        return (int) result;
    }

    default int sqlite3SetAuthorizer(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_set_authorizer")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3Strglob(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_strglob").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int strcspn(int arg0, int arg1) {
        long result = instance().exports().function("strcspn").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Strlike(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_strlike")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3AutoExtension(int arg0) {
        long result = instance().exports().function("sqlite3_auto_extension").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3CancelAutoExtension(int arg0) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_cancel_auto_extension")
                        .apply((long) arg0)[0];
        return (int) result;
    }

    default void sqlite3ResetAutoExtension() {
        instance().exports().function("sqlite3_reset_auto_extension").apply();
        return;
    }

    default int sqlite3Prepare(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_prepare")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3PrepareV3(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_prepare_v3")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5)[0];
        return (int) result;
    }

    default int sqlite3Prepare16(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_prepare16")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3Prepare16V2(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_prepare16_v2")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3Prepare16V3(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_prepare16_v3")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5)[0];
        return (int) result;
    }

    default int sqlite3GetTable(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_get_table")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5)[0];
        return (int) result;
    }

    default void sqlite3FreeTable(int arg0) {
        instance().exports().function("sqlite3_free_table").apply((long) arg0);
        return;
    }

    default int sqlite3CreateModule(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_module")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3CreateModuleV2(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_module_v2")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3DropModules(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_drop_modules")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3DeclareVtab(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_declare_vtab")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3VtabOnConflict(int arg0) {
        long result =
                instance().exports().function("sqlite3_vtab_on_conflict").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3VtabConfig(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vtab_config")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3VtabCollation(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vtab_collation")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3VtabIn(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vtab_in")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3VtabRhsValue(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_vtab_rhs_value")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3VtabDistinct(int arg0) {
        long result = instance().exports().function("sqlite3_vtab_distinct").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3KeywordName(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_keyword_name")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3KeywordCount() {
        long result = instance().exports().function("sqlite3_keyword_count").apply()[0];
        return (int) result;
    }

    default int sqlite3KeywordCheck(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_keyword_check")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Complete(int arg0) {
        long result = instance().exports().function("sqlite3_complete").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Complete16(int arg0) {
        long result = instance().exports().function("sqlite3_complete16").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Libversion() {
        long result = instance().exports().function("sqlite3_libversion").apply()[0];
        return (int) result;
    }

    default GlobalInstance sqlite3Version() {
        return instance().exports().global("sqlite3_version");
    }

    default int sqlite3LibversionNumber() {
        long result = instance().exports().function("sqlite3_libversion_number").apply()[0];
        return (int) result;
    }

    default int sqlite3Threadsafe() {
        long result = instance().exports().function("sqlite3_threadsafe").apply()[0];
        return (int) result;
    }

    default int sqlite3Shutdown() {
        long result = instance().exports().function("sqlite3_shutdown").apply()[0];
        return (int) result;
    }

    default GlobalInstance sqlite3DataDirectory() {
        return instance().exports().global("sqlite3_data_directory");
    }

    default GlobalInstance sqlite3TempDirectory() {
        return instance().exports().global("sqlite3_temp_directory");
    }

    default int sqlite3Config(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_config").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3DbMutex(int arg0) {
        long result = instance().exports().function("sqlite3_db_mutex").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3DbReleaseMemory(int arg0) {
        long result =
                instance().exports().function("sqlite3_db_release_memory").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3DbCacheflush(int arg0) {
        long result = instance().exports().function("sqlite3_db_cacheflush").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3DbConfig(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_db_config")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default long sqlite3LastInsertRowid(int arg0) {
        long result =
                instance().exports().function("sqlite3_last_insert_rowid").apply((long) arg0)[0];
        return result;
    }

    default void sqlite3SetLastInsertRowid(int arg0, long arg1) {
        instance().exports().function("sqlite3_set_last_insert_rowid").apply((long) arg0, arg1);
        return;
    }

    default long sqlite3Changes64(int arg0) {
        long result = instance().exports().function("sqlite3_changes64").apply((long) arg0)[0];
        return result;
    }

    default int sqlite3Changes(int arg0) {
        long result = instance().exports().function("sqlite3_changes").apply((long) arg0)[0];
        return (int) result;
    }

    default long sqlite3TotalChanges64(int arg0) {
        long result =
                instance().exports().function("sqlite3_total_changes64").apply((long) arg0)[0];
        return result;
    }

    default int sqlite3TotalChanges(int arg0) {
        long result = instance().exports().function("sqlite3_total_changes").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3TxnState(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_txn_state")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3Close(int arg0) {
        long result = instance().exports().function("sqlite3_close").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3CloseV2(int arg0) {
        long result = instance().exports().function("sqlite3_close_v2").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3BusyHandler(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_busy_handler")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default void sqlite3ProgressHandler(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("sqlite3_progress_handler")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default int sqlite3BusyTimeout(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_busy_timeout")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default void sqlite3Interrupt(int arg0) {
        instance().exports().function("sqlite3_interrupt").apply((long) arg0);
        return;
    }

    default int sqlite3IsInterrupted(int arg0) {
        long result = instance().exports().function("sqlite3_is_interrupted").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3CreateFunction(
            int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_function")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5,
                                (long) arg6,
                                (long) arg7)[0];
        return (int) result;
    }

    default int sqlite3CreateFunctionV2(
            int arg0,
            int arg1,
            int arg2,
            int arg3,
            int arg4,
            int arg5,
            int arg6,
            int arg7,
            int arg8) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_function_v2")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5,
                                (long) arg6,
                                (long) arg7,
                                (long) arg8)[0];
        return (int) result;
    }

    default int sqlite3CreateWindowFunction(
            int arg0,
            int arg1,
            int arg2,
            int arg3,
            int arg4,
            int arg5,
            int arg6,
            int arg7,
            int arg8,
            int arg9) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_window_function")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5,
                                (long) arg6,
                                (long) arg7,
                                (long) arg8,
                                (long) arg9)[0];
        return (int) result;
    }

    default int sqlite3CreateFunction16(
            int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_function16")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5,
                                (long) arg6,
                                (long) arg7)[0];
        return (int) result;
    }

    default int sqlite3OverloadFunction(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_overload_function")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3Trace(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_trace")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3TraceV2(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_trace_v2")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3Profile(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_profile")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3CommitHook(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_commit_hook")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3UpdateHook(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_update_hook")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3RollbackHook(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_rollback_hook")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3AutovacuumPages(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_autovacuum_pages")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3WalAutocheckpoint(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_wal_autocheckpoint")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3WalHook(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_wal_hook")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3WalCheckpoint(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_wal_checkpoint")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3WalCheckpointV2(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_wal_checkpoint_v2")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3ErrorOffset(int arg0) {
        long result = instance().exports().function("sqlite3_error_offset").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Errmsg16(int arg0) {
        long result = instance().exports().function("sqlite3_errmsg16").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Errcode(int arg0) {
        long result = instance().exports().function("sqlite3_errcode").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ExtendedErrcode(int arg0) {
        long result =
                instance().exports().function("sqlite3_extended_errcode").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3SystemErrno(int arg0) {
        long result = instance().exports().function("sqlite3_system_errno").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Errstr(int arg0) {
        long result = instance().exports().function("sqlite3_errstr").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Limit(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_limit")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3Open(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_open").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default void sqlite3FreeFilename(int arg0) {
        instance().exports().function("sqlite3_free_filename").apply((long) arg0);
        return;
    }

    default int sqlite3OpenV2(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_open_v2")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3Open16(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_open16").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3CreateCollation(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_collation")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3CreateCollationV2(
            int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_collation_v2")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5)[0];
        return (int) result;
    }

    default int sqlite3CreateCollation16(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_collation16")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3CollationNeeded(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_collation_needed")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3CollationNeeded16(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_collation_needed16")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sqlite3GetClientdata(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_get_clientdata")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3SetClientdata(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_set_clientdata")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int sqlite3GlobalRecover() {
        long result = instance().exports().function("sqlite3_global_recover").apply()[0];
        return (int) result;
    }

    default int sqlite3GetAutocommit(int arg0) {
        long result = instance().exports().function("sqlite3_get_autocommit").apply((long) arg0)[0];
        return (int) result;
    }

    default void sqlite3ThreadCleanup() {
        instance().exports().function("sqlite3_thread_cleanup").apply();
        return;
    }

    default int sqlite3TableColumnMetadata(
            int arg0,
            int arg1,
            int arg2,
            int arg3,
            int arg4,
            int arg5,
            int arg6,
            int arg7,
            int arg8) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_table_column_metadata")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5,
                                (long) arg6,
                                (long) arg7,
                                (long) arg8)[0];
        return (int) result;
    }

    default int sqlite3Sleep(int arg0) {
        long result = instance().exports().function("sqlite3_sleep").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3ExtendedResultCodes(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_extended_result_codes")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3TestControl(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_test_control")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3CreateFilename(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_create_filename")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int sqlite3UriKey(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_uri_key").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default long sqlite3UriInt64(int arg0, int arg1, long arg2) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_uri_int64")
                        .apply((long) arg0, (long) arg1, arg2)[0];
        return result;
    }

    default int strspn(int arg0, int arg1) {
        long result = instance().exports().function("strspn").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3FilenameDatabase(int arg0) {
        long result =
                instance().exports().function("sqlite3_filename_database").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3FilenameJournal(int arg0) {
        long result =
                instance().exports().function("sqlite3_filename_journal").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3FilenameWal(int arg0) {
        long result = instance().exports().function("sqlite3_filename_wal").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3DbName(int arg0, int arg1) {
        long result =
                instance().exports().function("sqlite3_db_name").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3DbFilename(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_db_filename")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3DbReadonly(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("sqlite3_db_readonly")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int sqlite3CompileoptionUsed(int arg0) {
        long result =
                instance().exports().function("sqlite3_compileoption_used").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3CompileoptionGet(int arg0) {
        long result =
                instance().exports().function("sqlite3_compileoption_get").apply((long) arg0)[0];
        return (int) result;
    }

    default int sqlite3Sourceid() {
        long result = instance().exports().function("sqlite3_sourceid").apply()[0];
        return (int) result;
    }

    default long lseek(int arg0, long arg1, int arg2) {
        long result =
                instance().exports().function("lseek").apply((long) arg0, arg1, (long) arg2)[0];
        return result;
    }

    default int open(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("open")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int sysconf(int arg0) {
        long result = instance().exports().function("sysconf").apply((long) arg0)[0];
        return (int) result;
    }

    default int strerror(int arg0) {
        long result = instance().exports().function("strerror").apply((long) arg0)[0];
        return (int) result;
    }

    default int utimes(int arg0, int arg1) {
        long result = instance().exports().function("utimes").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int strncmp(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("strncmp")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int strrchr(int arg0, int arg1) {
        long result = instance().exports().function("strrchr").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int localtime(int arg0) {
        long result = instance().exports().function("localtime").apply((long) arg0)[0];
        return (int) result;
    }

    default int memchr(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("memchr")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int malloc(int arg0) {
        long result = instance().exports().function("malloc").apply((long) arg0)[0];
        return (int) result;
    }

    default void free(int arg0) {
        instance().exports().function("free").apply((long) arg0);
        return;
    }

    default int realloc(int arg0, int arg1) {
        long result = instance().exports().function("realloc").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int close(int arg0) {
        long result = instance().exports().function("close").apply((long) arg0)[0];
        return (int) result;
    }

    default int access(int arg0, int arg1) {
        long result = instance().exports().function("access").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int getcwd(int arg0, int arg1) {
        long result = instance().exports().function("getcwd").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int stat(int arg0, int arg1) {
        long result = instance().exports().function("stat").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int fstat(int arg0, int arg1) {
        long result = instance().exports().function("fstat").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int ftruncate(int arg0, long arg1) {
        long result = instance().exports().function("ftruncate").apply((long) arg0, arg1)[0];
        return (int) result;
    }

    default int fcntl(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("fcntl")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int read(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("read")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int write(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("write")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int unlink(int arg0) {
        long result = instance().exports().function("unlink").apply((long) arg0)[0];
        return (int) result;
    }

    default int mkdir(int arg0, int arg1) {
        long result = instance().exports().function("mkdir").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int rmdir(int arg0) {
        long result = instance().exports().function("rmdir").apply((long) arg0)[0];
        return (int) result;
    }

    default int readlink(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("readlink")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int lstat(int arg0, int arg1) {
        long result = instance().exports().function("lstat").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default GlobalInstance HeapEnd() {
        return instance().exports().global("__heap_end");
    }

    default GlobalInstance HeapBase() {
        return instance().exports().global("__heap_base");
    }

    default int sbrk(int arg0) {
        long result = instance().exports().function("sbrk").apply((long) arg0)[0];
        return (int) result;
    }

    default int calloc(int arg0, int arg1) {
        long result = instance().exports().function("calloc").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int posixMemalign(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("posix_memalign")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int alignedAlloc(int arg0, int arg1) {
        long result =
                instance().exports().function("aligned_alloc").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int mallocUsableSize(int arg0) {
        long result = instance().exports().function("malloc_usable_size").apply((long) arg0)[0];
        return (int) result;
    }

    default int LibcMalloc(int arg0) {
        long result = instance().exports().function("__libc_malloc").apply((long) arg0)[0];
        return (int) result;
    }

    default void LibcFree(int arg0) {
        instance().exports().function("__libc_free").apply((long) arg0);
        return;
    }

    default int LibcCalloc(int arg0, int arg1) {
        long result =
                instance().exports().function("__libc_calloc").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int fdopendir(int arg0) {
        long result = instance().exports().function("fdopendir").apply((long) arg0)[0];
        return (int) result;
    }

    default int WasilibcNocwdOpendirat(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_opendirat")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcNocwdScandirat(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_scandirat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default GlobalInstance TlsBase() {
        return instance().exports().global("__tls_base");
    }

    default int WasiFdFdstatGet(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_fdstat_get")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiFdFdstatSetFlags(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_fdstat_set_flags")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcNocwdOpenatNomode(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_openat_nomode")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasilibcNocwdRenameat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_renameat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    // TODO: error in generation
    //    default void Exit(int arg0) {
    //        instance().exports().function("_Exit").apply((long) arg0);
    //        return;
    //    }
    //
    //    default void Exit(int arg0) {
    //        instance().exports().function("_exit").apply((long) arg0);
    //        return;
    //    }

    default int WasiFdFilestatGet(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_filestat_get")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcNocwdFstatat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_fstatat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasilibcNocwdMkdiratNomode(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_mkdirat_nomode")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcNocwdUtimensat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_utimensat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasiClockTimeGet(int arg0, long arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_clock_time_get")
                        .apply((long) arg0, arg1, (long) arg2)[0];
        return (int) result;
    }

    default GlobalInstance CLOCKREALTIME() {
        return instance().exports().global("_CLOCK_REALTIME");
    }

    default int clockNanosleep(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("clock_nanosleep")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int ClockNanosleep(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__clock_nanosleep")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default void Muloti4(int arg0, long arg1, long arg2, long arg3, long arg4, int arg5) {
        instance()
                .exports()
                .function("__muloti4")
                .apply((long) arg0, arg1, arg2, arg3, arg4, (long) arg5);
        return;
    }

    default int WasiPollOneoff(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_poll_oneoff")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasilibcNocwdFaccessat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_faccessat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasiFdSync(int arg0) {
        long result = instance().exports().function("__wasi_fd_sync").apply((long) arg0)[0];
        return (int) result;
    }

    default int WasiFdFilestatSetSize(int arg0, long arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_filestat_set_size")
                        .apply((long) arg0, arg1)[0];
        return (int) result;
    }

    default int WasilibcNocwdLinkat(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_linkat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default long Lseek(int arg0, long arg1, int arg2) {
        long result =
                instance().exports().function("__lseek").apply((long) arg0, arg1, (long) arg2)[0];
        return result;
    }

    default int WasiFdSeek(int arg0, long arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_seek")
                        .apply((long) arg0, arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasiFdRead(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_read")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasilibcNocwdReadlinkat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_readlinkat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasilibcNocwdSymlinkat(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd_symlinkat")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasiFdWrite(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_write")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasilibcIftodt(int arg0) {
        long result = instance().exports().function("__wasilibc_iftodt").apply((long) arg0)[0];
        return (int) result;
    }

    default int WasilibcDttoif(int arg0) {
        long result = instance().exports().function("__wasilibc_dttoif").apply((long) arg0)[0];
        return (int) result;
    }

    default int WasilibcFdRenumber(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_fd_renumber")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default void WasilibcPopulatePreopens() {
        instance().exports().function("__wasilibc_populate_preopens").apply();
        return;
    }

    default int WasiFdRenumber(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_renumber")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiFdClose(int arg0) {
        long result = instance().exports().function("__wasi_fd_close").apply((long) arg0)[0];
        return (int) result;
    }

    default void WasilibcEnsureEnviron() {
        instance().exports().function("__wasilibc_ensure_environ").apply();
        return;
    }

    default GlobalInstance WasilibcEnviron() {
        return instance().exports().global("__wasilibc_environ");
    }

    default void WasilibcInitializeEnviron() {
        instance().exports().function("__wasilibc_initialize_environ").apply();
        return;
    }

    default void WasilibcDeinitializeEnviron() {
        instance().exports().function("__wasilibc_deinitialize_environ").apply();
        return;
    }

    default void WasilibcMaybeReinitializeEnvironEagerly() {
        instance().exports().function("__wasilibc_maybe_reinitialize_environ_eagerly").apply();
        return;
    }

    default int WasiArgsGet(int arg0, int arg1) {
        long result =
                instance().exports().function("__wasi_args_get").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiArgsSizesGet(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_args_sizes_get")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiEnvironGet(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_environ_get")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiEnvironSizesGet(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_environ_sizes_get")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiClockResGet(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_clock_res_get")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiFdAdvise(int arg0, long arg1, long arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_advise")
                        .apply((long) arg0, arg1, arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasiFdAllocate(int arg0, long arg1, long arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_allocate")
                        .apply((long) arg0, arg1, arg2)[0];
        return (int) result;
    }

    default int WasiFdDatasync(int arg0) {
        long result = instance().exports().function("__wasi_fd_datasync").apply((long) arg0)[0];
        return (int) result;
    }

    default int WasiFdFdstatSetRights(int arg0, long arg1, long arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_fdstat_set_rights")
                        .apply((long) arg0, arg1, arg2)[0];
        return (int) result;
    }

    default int WasiFdFilestatSetTimes(int arg0, long arg1, long arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_filestat_set_times")
                        .apply((long) arg0, arg1, arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasiFdPread(int arg0, int arg1, int arg2, long arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_pread")
                        .apply((long) arg0, (long) arg1, (long) arg2, arg3, (long) arg4)[0];
        return (int) result;
    }

    default int WasiFdPrestatGet(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_prestat_get")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiFdPrestatDirName(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_prestat_dir_name")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasiFdPwrite(int arg0, int arg1, int arg2, long arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_pwrite")
                        .apply((long) arg0, (long) arg1, (long) arg2, arg3, (long) arg4)[0];
        return (int) result;
    }

    default int WasiFdReaddir(int arg0, int arg1, int arg2, long arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_fd_readdir")
                        .apply((long) arg0, (long) arg1, (long) arg2, arg3, (long) arg4)[0];
        return (int) result;
    }

    default int WasiFdTell(int arg0, int arg1) {
        long result =
                instance().exports().function("__wasi_fd_tell").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiPathCreateDirectory(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_create_directory")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiPathFilestatGet(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_filestat_get")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasiPathFilestatSetTimes(
            int arg0, int arg1, int arg2, long arg3, long arg4, int arg5) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_filestat_set_times")
                        .apply((long) arg0, (long) arg1, (long) arg2, arg3, arg4, (long) arg5)[0];
        return (int) result;
    }

    default int WasiPathLink(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_link")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int WasiPathOpen(
            int arg0, int arg1, int arg2, int arg3, long arg4, long arg5, int arg6, int arg7) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_open")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                arg4,
                                arg5,
                                (long) arg6,
                                (long) arg7)[0];
        return (int) result;
    }

    default int WasiPathReadlink(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_readlink")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int WasiPathRemoveDirectory(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_remove_directory")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiPathRename(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_rename")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasiPathSymlink(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_symlink")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasiPathUnlinkFile(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_path_unlink_file")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default void WasiProcExit(int arg0) {
        instance().exports().function("__wasi_proc_exit").apply((long) arg0);
        return;
    }

    default int WasiSchedYield() {
        long result = instance().exports().function("__wasi_sched_yield").apply()[0];
        return (int) result;
    }

    default int WasiRandomGet(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_random_get")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasiSockAccept(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_sock_accept")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasiSockRecv(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_sock_recv")
                        .apply(
                                (long) arg0,
                                (long) arg1,
                                (long) arg2,
                                (long) arg3,
                                (long) arg4,
                                (long) arg5)[0];
        return (int) result;
    }

    default int WasiSockSend(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_sock_send")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int WasiSockShutdown(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasi_sock_shutdown")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcNocwdWasilibcRmdirat(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd___wasilibc_rmdirat")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcNocwdWasilibcUnlinkat(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_nocwd___wasilibc_unlinkat")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default void abort() {
        instance().exports().function("abort").apply();
        return;
    }

    default int openat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("openat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int symlinkat(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("symlinkat")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int readlinkat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("readlinkat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int mkdirat(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("mkdirat")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int opendirat(int arg0, int arg1) {
        long result = instance().exports().function("opendirat").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int scandirat(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("scandirat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int faccessat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("faccessat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int fstatat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("fstatat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int utimensat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("utimensat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int linkat(int arg0, int arg1, int arg2, int arg3, int arg4) {
        long result =
                instance()
                        .exports()
                        .function("linkat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4)[0];
        return (int) result;
    }

    default int renameat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("renameat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasilibcUnlinkat(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_unlinkat")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcRmdirat(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_rmdirat")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default GlobalInstance WasilibcCwd() {
        return instance().exports().global("__wasilibc_cwd");
    }

    default int strdup(int arg0) {
        long result = instance().exports().function("strdup").apply((long) arg0)[0];
        return (int) result;
    }

    default int strcpy(int arg0, int arg1) {
        long result = instance().exports().function("strcpy").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcFindRelpath(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_find_relpath")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasilibcOpenNomode(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_open_nomode")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int utime(int arg0, int arg1) {
        long result = instance().exports().function("utime").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int remove(int arg0) {
        long result = instance().exports().function("remove").apply((long) arg0)[0];
        return (int) result;
    }

    default int opendir(int arg0) {
        long result = instance().exports().function("opendir").apply((long) arg0)[0];
        return (int) result;
    }

    default int scandir(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("scandir")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int symlink(int arg0, int arg1) {
        long result = instance().exports().function("symlink").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcAccess(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_access")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasilibcStat(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_stat")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasilibcUtimens(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_utimens")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasilibcLink(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_link")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasilibcLinkNewat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_link_newat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int WasilibcLinkOldat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_link_oldat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int rename(int arg0, int arg1) {
        long result = instance().exports().function("rename").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcRenameNewat(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_rename_newat")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int WasilibcRenameOldat(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_rename_oldat")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default void qsort(int arg0, int arg1, int arg2, int arg3) {
        instance()
                .exports()
                .function("qsort")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3);
        return;
    }

    default int link(int arg0, int arg1) {
        long result = instance().exports().function("link").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int chmod(int arg0, int arg1) {
        long result = instance().exports().function("chmod").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int fchmod(int arg0, int arg1) {
        long result = instance().exports().function("fchmod").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int fchmodat(int arg0, int arg1, int arg2, int arg3) {
        long result =
                instance()
                        .exports()
                        .function("fchmodat")
                        .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3)[0];
        return (int) result;
    }

    default int statvfs(int arg0, int arg1) {
        long result = instance().exports().function("statvfs").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int fstatvfs(int arg0, int arg1) {
        long result = instance().exports().function("fstatvfs").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcRegisterPreopenedFd(int arg0, int arg1) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_register_preopened_fd")
                        .apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int WasilibcFindAbspath(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__wasilibc_find_abspath")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default void WasilibcResetPreopens() {
        instance().exports().function("__wasilibc_reset_preopens").apply();
        return;
    }

    default int clearenv() {
        long result = instance().exports().function("clearenv").apply()[0];
        return (int) result;
    }

    default void EnvRmAdd(int arg0, int arg1) {
        instance().exports().function("__env_rm_add").apply((long) arg0, (long) arg1);
        return;
    }

    default int Strchrnul(int arg0, int arg1) {
        long result =
                instance().exports().function("__strchrnul").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int StrerrorL(int arg0, int arg1) {
        long result =
                instance().exports().function("__strerror_l").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int Lctrans(int arg0, int arg1) {
        long result = instance().exports().function("__lctrans").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default GlobalInstance Libc() {
        return instance().exports().global("__libc");
    }

    default int strerrorL(int arg0, int arg1) {
        long result =
                instance().exports().function("strerror_l").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default GlobalInstance Progname() {
        return instance().exports().global("__progname");
    }

    default GlobalInstance PrognameFull() {
        return instance().exports().global("__progname_full");
    }

    default GlobalInstance Hwcap() {
        return instance().exports().global("__hwcap");
    }

    default GlobalInstance programInvocationShortName() {
        return instance().exports().global("program_invocation_short_name");
    }

    default GlobalInstance programInvocationName() {
        return instance().exports().global("program_invocation_name");
    }

    default int LctransImpl(int arg0, int arg1) {
        long result =
                instance().exports().function("__lctrans_impl").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int LctransCur(int arg0) {
        long result = instance().exports().function("__lctrans_cur").apply((long) arg0)[0];
        return (int) result;
    }

    default void QsortR(int arg0, int arg1, int arg2, int arg3, int arg4) {
        instance()
                .exports()
                .function("__qsort_r")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4);
        return;
    }

    default void qsortR(int arg0, int arg1, int arg2, int arg3, int arg4) {
        instance()
                .exports()
                .function("qsort_r")
                .apply((long) arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4);
        return;
    }

    default int Memrchr(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("__memrchr")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int memrchr(int arg0, int arg1, int arg2) {
        long result =
                instance()
                        .exports()
                        .function("memrchr")
                        .apply((long) arg0, (long) arg1, (long) arg2)[0];
        return (int) result;
    }

    default int Stpcpy(int arg0, int arg1) {
        long result = instance().exports().function("__stpcpy").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int stpcpy(int arg0, int arg1) {
        long result = instance().exports().function("stpcpy").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int strchrnul(int arg0, int arg1) {
        long result = instance().exports().function("strchrnul").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int SecsToTm(long arg0, int arg1) {
        long result = instance().exports().function("__secs_to_tm").apply(arg0, (long) arg1)[0];
        return (int) result;
    }

    default void SecsToZone(long arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        instance()
                .exports()
                .function("__secs_to_zone")
                .apply(arg0, (long) arg1, (long) arg2, (long) arg3, (long) arg4, (long) arg5);
        return;
    }

    default GlobalInstance Utc() {
        return instance().exports().global("__utc");
    }

    default int TmToTzname(int arg0) {
        long result = instance().exports().function("__tm_to_tzname").apply((long) arg0)[0];
        return (int) result;
    }

    default int LocaltimeR(int arg0, int arg1) {
        long result =
                instance().exports().function("__localtime_r").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default int localtimeR(int arg0, int arg1) {
        long result =
                instance().exports().function("localtime_r").apply((long) arg0, (long) arg1)[0];
        return (int) result;
    }

    default void Ashlti3(int arg0, long arg1, long arg2, int arg3) {
        instance().exports().function("__ashlti3").apply((long) arg0, arg1, arg2, (long) arg3);
        return;
    }

    default void Udivti3(int arg0, long arg1, long arg2, long arg3, long arg4) {
        instance().exports().function("__udivti3").apply((long) arg0, arg1, arg2, arg3, arg4);
        return;
    }

    default void Multi3(int arg0, long arg1, long arg2, long arg3, long arg4) {
        instance().exports().function("__multi3").apply((long) arg0, arg1, arg2, arg3, arg4);
        return;
    }

    default void Udivmodti4(int arg0, long arg1, long arg2, long arg3, long arg4, int arg5) {
        instance()
                .exports()
                .function("__udivmodti4")
                .apply((long) arg0, arg1, arg2, arg3, arg4, (long) arg5);
        return;
    }

    default GlobalInstance DsoHandle() {
        return instance().exports().global("__dso_handle");
    }

    default GlobalInstance DataEnd() {
        return instance().exports().global("__data_end");
    }

    default GlobalInstance StackLow() {
        return instance().exports().global("__stack_low");
    }

    default GlobalInstance StackHigh() {
        return instance().exports().global("__stack_high");
    }

    default GlobalInstance GlobalBase() {
        return instance().exports().global("__global_base");
    }

    default GlobalInstance TableBase() {
        return instance().exports().global("__table_base");
    }
}
