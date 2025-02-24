#! /bin/bash
set -euxo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# TODO: improve this script and automate it fully

# PREPARATION: into this script folder

# Download WASI-SDK
# rm -f wasi-sdk-24.0-x86_64-linux.tar.gz
# rm -rf wasi-sdk
# wget https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-25/wasi-sdk-25.0-x86_64-linux.tar.gz
# tar -xvf wasi-sdk-25.0-x86_64-linux.tar.gz
# mv wasi-sdk-25.0-x86_64-linux wasi-sdk
# rm wasi-sdk-25.0-x86_64-linux.tar.gz

# Download the latest SQLite amalgamation 3.48.0 as of today
# rm -rf sqlite-amalgamation
# wget https://www.sqlite.org/2025/sqlite-amalgamation-3490100.zip
# unzip sqlite-amalgamation-3490100.zip
# mv sqlite-amalgamation-3490100 sqlite-amalgamation
# rm sqlite-amalgamation-3490100.zip

# Download and install Binaryen for optimizations
# rm -rf binaryen
# wget https://github.com/WebAssembly/binaryen/releases/download/version_121/binaryen-version_121-x86_64-linux.tar.gz
# tar -xvf binaryen-version_121-x86_64-linux.tar.gz
# mv binaryen-version_121 binaryen
# rm binaryen-version_121-x86_64-linux.tar.gz

export WASI_SDK_PATH=${SCRIPT_DIR}/wasi-sdk

rm -f ${SCRIPT_DIR}/libsqlite3.wasm
rm -f ${SCRIPT_DIR}/libsqlite3-opt.wasm

rm -f ${SCRIPT_DIR}/sqlite-amalgamation/sqlite3_helpers.c
cp ${SCRIPT_DIR}/sqlite3_helpers.c ${SCRIPT_DIR}/sqlite-amalgamation/

(
    cd ${SCRIPT_DIR}/sqlite-amalgamation
    ${WASI_SDK_PATH}/bin/clang --sysroot=${WASI_SDK_PATH}/share/wasi-sysroot \
      --target=wasm32-wasi \
      -o ../libsqlite3.wasm \
      sqlite3.c sqlite3_helpers.c \
      -Wl,--export-all \
      -Wl,--import-undefined \
      -Wl,--no-entry \
      -Wl,--initial-memory=32768000 \
      -Wl,--stack-first \
      -Wl,--strip-debug \
      -mnontrapping-fptoint -msign-ext \
      -fno-stack-protector -fno-stack-clash-protection \
      -mmutable-globals -mmultivalue \
      -mbulk-memory -mreference-types \
      -mexec-model=reactor \
      -g0 -Oz \
      -DSQLITE_ENABLE_LOAD_EXTENSION=0 \
      -DSQLITE_HAVE_ISNAN=1 \
      -DHAVE_USLEEP=1 \
      -DSQLITE_ENABLE_COLUMN_METADATA \
      -DSQLITE_CORE \
      -DSQLITE_ENABLE_FTS3 \
      -DSQLITE_ENABLE_FTS3_PARENTHESIS \
      -DSQLITE_ENABLE_FTS5 \
      -DSQLITE_ENABLE_RTREE \
      -DSQLITE_ENABLE_STAT4 \
      -DSQLITE_ENABLE_DBSTAT_VTAB \
      -DSQLITE_ENABLE_MATH_FUNCTIONS \
      -DSQLITE_DEFAULT_MEMSTATUS=0 \
      -DSQLITE_DEFAULT_FILE_PERMISSIONS=0666 \
      -DSQLITE_MAX_VARIABLE_NUMBER=250000 \
      -DSQLITE_MAX_LENGTH=2147483647 \
      -DSQLITE_MAX_COLUMN=32767 \
      -DSQLITE_MAX_SQL_LENGTH=1073741824 \
      -DSQLITE_MAX_FUNCTION_ARG=127 \
      -DSQLITE_MAX_ATTACHED=125 \
      -DSQLITE_MAX_PAGE_COUNT=4294967294 \
      -DSQLITE_DISABLE_PAGECACHE_OVERFLOW_STATS \
      -DSQLITE_USE_ALLOCA=0 \
      -DSQLITE_4_BYTE_ALIGNED_MALLOC=1 \
      -DSQLITE_32BIT_ROWID=1 \
      -DSQLITE_DEFAULT_LOCKING_MODE=0 \
      -DSQLITE_THREADSAFE=0 \
      -DSQLITE_OMIT_SHARED_CACHE=1

    # -DSQLITE_OMIT_WAL=0 \
    # this shows more errors on varous tests -> is it useful to include?
    # -DSQLITE_MEMDEBUG=1

    # Use ALLOCA is an experiment, verify
    # SQLITE_OMIT_LOAD_EXTENSION -> doesn't seems to be used in the WASI build
    # Options that would not work on Wasm
        # -DSQLITE_MAX_MMAP_SIZE=1099511627776 \
        # -DSQLITE_THREADSAFE=1 \
)

# This step seems not needed after using "-Wl,--strip-debug"
# 866873 libsqlite3-opt.wasm
# 864174 libsqlite3.wasm
# TODO: re-evaluate this usage
# ${SCRIPT_DIR}/binaryen/bin/wasm-opt -g --strip --strip-producers -c -O3 \
#     ${SCRIPT_DIR}/libsqlite3.wasm -o ${SCRIPT_DIR}/libsqlite3-opt.wasm \
# 	--enable-mutable-globals --enable-multivalue \
# 	--enable-bulk-memory --enable-reference-types \
# 	--enable-nontrapping-float-to-int --enable-sign-ext
