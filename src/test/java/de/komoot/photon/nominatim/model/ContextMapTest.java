package de.komoot.photon.nominatim.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class ContextMapTest {
        private ContextMap testMap() {
        ContextMap map = new ContextMap();
        map.addName("default", "n1");
        map.addName("default", "n2");
        map.addName("old", "former");
        return map;
    }

    @Test
    void testAddName() {
        ContextMap map = new ContextMap();

        assertThat(map).isEmpty();

        map.addName("default", "something");
        assertThat(map).isEqualTo(Map.of("default", Set.of("something")));

        map.addName("alt", "else");
        assertThat(map).isEqualTo(Map.of(
                "default", Set.of("something"),
                "alt", Set.of("else")));

        map.addName("default", "45");
        assertThat(map).isEqualTo(Map.of(
                "default", Set.of("something", "45"),
                "alt", Set.of("else")));

        map.addName("alt", "else");
        assertThat(map).isEqualTo(Map.of(
                "default", Set.of("something", "45"),
                "alt", Set.of("else")));
    }

    @Test
    void testAddFromSimpleMap() {
        ContextMap map = testMap();

        map.addAll(Map.of("alt", "XX", "default", "n3", "old", "former"));

        assertThat(map).isEqualTo(Map.of(
                "default", Set.of("n1", "n2", "n3"),
                "alt", Set.of("XX"),
                "old", Set.of("former")));
    }

    @Test
    void testAddFromContextMap() {
        ContextMap map = testMap();

        ContextMap other = new ContextMap();
        other.addName("default", "n1");
        other.addName("default", "n3");
        other.addName("alt", "XX");
        other.addName("alt", "YY");

        map.addAll(other);
        assertThat(map).isEqualTo(Map.of(
                "default", Set.of("n1", "n2", "n3"),
                "alt", Set.of("XX", "YY"),
                "old", Set.of("former")));

        map.addName("alt", "ZZ");
        assertThat(map.get("alt")).isEqualTo(Set.of("XX", "YY", "ZZ"));
        assertThat(other.get("alt")).isEqualTo(Set.of("XX", "YY"));
    }
}
