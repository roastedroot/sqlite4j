/*--------------------------------------------------------------------------
 *  Copyright 2007 Taro L. Saito
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *--------------------------------------------------------------------------*/
// --------------------------------------
// SQLite JDBC Project
//
// SQLite.java
// Since: 2007/05/10
//
// $URL$
// $Author$
// --------------------------------------
package com.github.andreaTP.sqlite.wasm;

import com.github.andreaTP.sqlite.wasm.core.WasmDB;
import com.github.andreaTP.sqlite.wasm.util.Logger;
import com.github.andreaTP.sqlite.wasm.util.LoggerFactory;
import com.github.andreaTP.sqlite.wasm.util.OSInfo;

/**
 * Set the system properties, org.sqlite.lib.path, org.sqlite.lib.name, appropriately so that the
 * SQLite JDBC driver can find *.dll, *.dylib and *.so files, according to the current OS (win,
 * linux, mac).
 *
 * <p>The library files are automatically extracted from this project's package (JAR).
 *
 * <p>usage: call {@link #initialize()} before using SQLite JDBC driver.
 *
 * @author leo
 */
// TODO: clean this file up from everything not needed
public class SQLiteJDBCLoader {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteJDBCLoader.class);

    private static boolean loaded = false;

    @SuppressWarnings("unused")
    private static void getNativeLibraryFolderForTheCurrentOS() {
        String osName = OSInfo.getOSName();
        String archName = OSInfo.getArchName();
    }

    /**
     * @return The major version of the SQLite JDBC driver.
     */
    public static int getMajorVersion() {
        String[] c = getVersion().split("\\.");
        return (c.length > 0) ? Integer.parseInt(c[0]) : 1;
    }

    /**
     * @return The minor version of the SQLite JDBC driver.
     */
    public static int getMinorVersion() {
        String[] c = getVersion().split("\\.");
        return (c.length > 1) ? Integer.parseInt(c[1]) : 0;
    }

    /**
     * @return The version of the SQLite JDBC driver.
     */
    public static String getVersion() {

        // TODO: FIXME: inject at compile time with a template?
        return WasmDB.version();
        //        return "unknown";
    }
}
