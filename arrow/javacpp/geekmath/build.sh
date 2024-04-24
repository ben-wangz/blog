#! /bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

mkdir -p $SCRIPT_DIR/cpp/build
cd $SCRIPT_DIR/cpp/build
cmake ..
make
cd -

PLATFORM=linux-x86_64
mkdir -p $SCRIPT_DIR/build/$PLATFORM/include
cp $SCRIPT_DIR/cpp/src/include/geekmath.hpp $SCRIPT_DIR/build/$PLATFORM/include/geekmath.hpp
mkdir -p $SCRIPT_DIR/build/$PLATFORM/lib
cp $SCRIPT_DIR/cpp/build/libgeekmathLib.so $SCRIPT_DIR/build/$PLATFORM/lib/libgeekmathLib.so
