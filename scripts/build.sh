#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf target/classes
mkdir -p target/classes
javac --release 21 -d target/classes $(find src/main/java -name '*.java')
jar --create --file target/backend-operations-lab.jar \
    --main-class com.studyforge.lab.BackendOperationsApplication \
    -C target/classes .
