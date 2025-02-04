package io.roastedroot.sqlite4j;

/** https://www.sqlite.org/c3ref/commit_hook.html */
public interface SQLiteCommitListener {

    void onCommit();

    void onRollback();
}
