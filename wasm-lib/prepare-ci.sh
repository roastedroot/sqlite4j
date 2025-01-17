#! /bin/bash
set -euxo pipefail

rm -f wasi-sdk-24.0-x86_64-linux.tar.gz
rm -rf wasi-sdk
wget https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-25/wasi-sdk-25.0-x86_64-linux.tar.gz
tar -xvf wasi-sdk-25.0-x86_64-linux.tar.gz
mv wasi-sdk-25.0-x86_64-linux wasi-sdk
rm wasi-sdk-25.0-x86_64-linux.tar.gz

rm -rf sqlite-amalgamation
wget https://www.sqlite.org/2025/sqlite-amalgamation-3480000.zip
unzip sqlite-amalgamation-3480000.zip
mv sqlite-amalgamation-3480000 sqlite-amalgamation
rm sqlite-amalgamation-3480000.zip

rm -rf binaryen
wget https://github.com/WebAssembly/binaryen/releases/download/version_121/binaryen-version_121-x86_64-linux.tar.gz
tar -xvf binaryen-version_121-x86_64-linux.tar.gz
mv binaryen-version_121 binaryen
rm binaryen-version_121-x86_64-linux.tar.gz
