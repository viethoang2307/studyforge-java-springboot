#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
rm -rf out logs
mkdir -p out logs

javac -g -d out src/*.java

echo '=== bytecode excerpt ==='
javap -classpath out -c -p JvmLifecycleDemo | sed -n '1,100p'

echo '=== class loading, JIT and GC ==='
java -Xlog:class+load=info -XX:+PrintCompilation \
  -Xlog:gc*:file=logs/gc.log:time,uptime,level,tags \
  -cp out JvmLifecycleDemo 2000000 >logs/runtime.log 2>&1

echo 'Created logs/runtime.log and logs/gc.log'
echo 'Run the destructive examples explicitly with:'
echo '  java -Xss256k -cp out StackOverflowDemo --run'
echo '  java -Xms32m -Xmx32m -Xlog:gc* -cp out HeapOomDemo --run'
