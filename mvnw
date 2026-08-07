#!/bin/sh
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
VERSION=3.9.9
DIST="$BASE_DIR/.mvn/wrapper/apache-maven-$VERSION"
if [ ! -x "$DIST/bin/mvn" ]; then mkdir -p "$BASE_DIR/.mvn/wrapper"; curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$VERSION/apache-maven-$VERSION-bin.tar.gz" | tar -xz -C "$BASE_DIR/.mvn/wrapper"; fi
exec "$DIST/bin/mvn" "$@"
