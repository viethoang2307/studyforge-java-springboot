package dev.studyforge.demo;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MutableHashKeyDemoTest {
    @Test
    void changingAHashedFieldMakesSetEntryUnreachable() {
        MutableHashKeyDemo.MutableKey key = new MutableHashKeyDemo.MutableKey("before");
        Set<MutableHashKeyDemo.MutableKey> keys = new HashSet<>();
        keys.add(key);

        key.setValue("after");

        assertSame(key, keys.iterator().next(), "the reference is physically still stored");
        assertFalse(keys.contains(key), "lookup starts in the new hash bucket");
        assertFalse(keys.remove(key), "normal removal cannot find the old bucket");
    }

    @Test
    void changingAHashedFieldMakesMapValueUnreachable() {
        MutableHashKeyDemo.MutableKey key = new MutableHashKeyDemo.MutableKey("before");
        Map<MutableHashKeyDemo.MutableKey, String> values = new HashMap<>();
        values.put(key, "stored value");

        key.setValue("after");

        assertTrue(values.entrySet().stream().anyMatch(entry -> entry.getKey() == key));
        assertNull(values.get(key));
    }
}
