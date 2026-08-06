#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
build_dir="$(mktemp -d)"
trap 'rm -rf "$build_dir"' EXIT

javac -d "$build_dir" "$script_dir"/*.java

for demo in \
    PrimitiveAndReferenceDemo \
    EqualityAndMutabilityDemo \
    PassByValueDemo \
    BoxingDemo \
    NumberPitfallsDemo; do
    echo "=== $demo ==="
    java -ea -cp "$build_dir" "$demo"
done
