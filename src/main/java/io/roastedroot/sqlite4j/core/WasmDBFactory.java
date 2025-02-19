package io.roastedroot.sqlite4j.core;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.roastedroot.sqlite4j.SQLiteConfig;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.sql.SQLException;

// This class serve to retrieve the right WasmDB instance to the connection
public class WasmDBFactory {
    // One single filesystem for all connections
    // is always needed for backup and restores
    private FileSystem fs = null;
    private int instancesCount = 0;

    public WasmDBFactory() {}

    public WasmDB create(String url, String fileName, SQLiteConfig config, boolean isMemory)
            throws SQLException {
        // lazily initialized at the opening of the first connection
        if (fs == null) {
            fs =
                    Jimfs.newFileSystem(
                            Configuration.unix().toBuilder().setAttributeViews("unix").build());
        }

        instancesCount++;
        return new WasmDB(fs, url, fileName, config, isMemory);
    }

    public void close(WasmDB db) throws SQLException {
        db.close();
        instancesCount--;

        if (instancesCount <= 0) {
            try {
                fs.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close the shared VirtualFileSystem", e);
            }
            fs = null;
        }
    }
}
