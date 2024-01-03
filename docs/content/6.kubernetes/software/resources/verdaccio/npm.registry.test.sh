#!/bin/bash

set -e
set -x

echo "registry=https://verdaccio.local" > $HOME/.npmrc
echo "strict-ssl=false" >> $HOME/.npmrc
npm install -g @vue/cli --loglevel verbose
vue create --default --registry https://verdaccio.local --packageManager "npm" --no-git helloworld
rm -rf helloworld/node_modules helloworld/package-lock.json
cd helloworld
npm install --loglevel verbose
npm run build --loglevel verbose
sed -i -Ee 's/^( *"private": *)true( *,)/\1false\2/' package.json
sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && apk update && apk upgrade && apk add expect
npm publish || echo "expected failed: need auth"
expect /app/npm.login.expect $NPM_SCOPE_OPTION $NPM_ADMIN_USERNAME $NPM_ADMIN_PASSWORD $NPM_LOGIN_EMAIL $NPM_REGISTRY
npm publish && echo "publish succeed"
