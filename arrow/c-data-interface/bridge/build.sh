#! /bin/bash

set -e
set -x

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

bash $SCRIPT_DIR/fedora-38-arrow-install.sh

rm -rf $SCRIPT_DIR/cpp/build
mkdir -p $SCRIPT_DIR/cpp/build
cd $SCRIPT_DIR/cpp/build
cmake ..
make
cd -

PLATFORM=linux-x86_64
mkdir -p $SCRIPT_DIR/build/$PLATFORM/include
cp $SCRIPT_DIR/cpp/src/include/CDataCppBridge.h $SCRIPT_DIR/build/$PLATFORM/include/CDataCppBridge.h
mkdir -p $SCRIPT_DIR/build/$PLATFORM/lib
cp $SCRIPT_DIR/cpp/build/libCDataCppBridgeLib.so $SCRIPT_DIR/build/$PLATFORM/lib/libCDataCppBridgeLib.so
