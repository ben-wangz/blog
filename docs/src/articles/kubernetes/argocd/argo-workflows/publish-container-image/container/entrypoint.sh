#! /bin/sh

MAX_TRIES=${1:-0}
aria2c -i /app/files.list.aria2 --max-tries=${MAX_TRIES} --continue "${@:2}"