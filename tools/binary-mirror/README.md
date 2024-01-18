# binary mirror

## what
* mirror binary files from remote to local by aria2

## usage
* build images
    + ```shell
      bash transformer/build.sh
      bash downloader/build.sh
      ```
* prepare [files.list](files.list.example)
* transform `file.list.example` to `file.list.aria2`
    + ```shell
      bash transform.sh files.list.example
      ```
* download files
    + ```shell
      bash download.sh
      ```
* serve files
    + ```shell
      bash service/start.sh
      ```