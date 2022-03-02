#!/bin/bash

set -e
set -x

mkdir -p /app/lib-example
gradle init --type java-library \
    --project-dir /app/lib-example  \
    --dsl groovy \
    --test-framework junit-jupiter \
    --project-name java-lib-example \
    --package java.lib.example \
    --incubating
gradle wrapper --project-dir /app/lib-example --gradle-version 7.4
cp /app/build.gradle /app/lib-example/lib/build.gradle
sed -i "s/admin-password-which-needs-to-be-replaced/$ADMIN_PASSWORD/" /app/lib-example/lib/build.gradle
cd /app/lib-example && ./gradlew clean build publish --exclude-task test && cd -
