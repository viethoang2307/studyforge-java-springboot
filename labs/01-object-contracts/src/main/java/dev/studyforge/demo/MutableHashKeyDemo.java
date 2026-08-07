package dev.studyforge.demo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Demonstrates why fields used by equals/hashCode must not change while used as hash keys. */
public final class MutableHashKeyDemo {
    private MutableHashKeyDemo() {
    }

    public static void main(String[] args) {
        MutableKey key = new MutableKey("before");
        Set<MutableKey> set = new HashSet<>();
        Map<MutableKey, String> map = new HashMap<>();
        set.add(key);
        map.put(key, "stored value");

        key.setValue("after");

        System.out.println("Same reference is still in iteration: " + set.iterator().next().equals(key));
        System.out.println("HashSet.contains(key): " + set.contains(key));
        System.out.println("HashMap.get(key): " + map.get(key));
        System.out.println("HashSet.remove(key): " + set.remove(key));
    }

    public static final class MutableKey {
        private String value;

        public MutableKey(String value) {
            this.value = Objects.requireNonNull(value);
        }

        public void setValue(String value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            return other instanceof MutableKey key && value.equals(key.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
