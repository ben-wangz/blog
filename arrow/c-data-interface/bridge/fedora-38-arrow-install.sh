#! /bin/bash

set -e
set -x
sudo dnf install -y libarrow-devel libarrow-glib-devel \
    libarrow-dataset-devel libarrow-dataset-glib-devel \
    libarrow-flight-devel \
    parquet-libs-devel \
    parquet-glib-devel
