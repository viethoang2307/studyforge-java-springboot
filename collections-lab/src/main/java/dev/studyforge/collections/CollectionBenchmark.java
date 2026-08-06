package dev.studyforge.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class CollectionBenchmark {

    @State(Scope.Thread)
    public static class Data {
        @Param({"100", "10000"})
        int size;
        List<Integer> arrayList;
        List<Integer> linkedList;
        Map<Integer, Integer> hashMap;

        @Setup(Level.Trial)
        public void setup() {
            arrayList = new ArrayList<>(size);
            linkedList = new LinkedList<>();
            hashMap = new HashMap<>(capacityFor(size));
            for (int i = 0; i < size; i++) {
                arrayList.add(i);
                linkedList.add(i);
                hashMap.put(i, i);
            }
        }
    }

    @Benchmark
    public int arrayListRandomGet(Data data) {
        return data.arrayList.get(data.size / 2);
    }

    @Benchmark
    public int linkedListRandomGet(Data data) {
        return data.linkedList.get(data.size / 2);
    }

    @Benchmark
    public void arrayListIteration(Data data, Blackhole blackhole) {
        data.arrayList.forEach(blackhole::consume);
    }

    @Benchmark
    public void linkedListIteration(Data data, Blackhole blackhole) {
        data.linkedList.forEach(blackhole::consume);
    }

    @Benchmark
    public int hashMapLookup(Data data) {
        return data.hashMap.get(data.size / 2);
    }

    private static int capacityFor(int entries) {
        return (int) Math.ceil(entries / 0.75d);
    }
}
