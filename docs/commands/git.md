### init global config

```shell
git config --global user.name "ben.wangz"
git config --global user.email ben.wangz@foxmail.com
git config --global pager.branch false
git config --global pull.ff only
git --no-pager diff
```

### clean local-remote-branches which are deleted by remote

```shell
git remote prune origin
```

### get specific file from remote

* from remote: git@github.com:ben-wangz/blog.git
* from branch: master
* from path: docs/commands
* specific file: git.md
* write as tar gz file: git.md.tar.gz

* ```shell
  git archive --remote=git@github.com:ben-wangz/blog.git master:docs/commands build.gradle -o git.md.tar.gz
  ```