package de.komoot.photon.json;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
class AddressMap {
    private final List<Map.Entry<String, String>> address = new ArrayList<>();

    @JsonAnySetter
    public void add(String key, Object value) {
        if (value instanceof String) {
            address.add(new AbstractMap.SimpleEntry<>(key, (String) value));
        } else if (value instanceof List<?>) {
            for (var ele : ((List<?>) value)) {
                if (ele instanceof String) {
                    address.add(new AbstractMap.SimpleEntry<>(key, (String) ele));
                }
            }
        }
    }

    List<Map.Entry<String, String>> get() {
        return address;
    }

    Map<String, String> getMainAddress() {
        Map<String, String> out = new HashMap<>();
        for (var entry : address) {
            out.putIfAbsent(entry.getKey(), entry.getValue());
        }

        return out;
    }
}
