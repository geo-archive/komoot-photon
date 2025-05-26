package de.komoot.photon.nominatim.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NameMap extends AbstractMap<String, String> {
    private final Set<Entry<String, String>> entries = new HashSet<>();

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return entries;
    }

    public static NameMap makeForPlace(Map<String, String> source, String[] languages) {
        return new NameMap()
                .setLocaleNames(source, languages)
                .setName("alt", source, "_place_alt_name", "alt_name")
                .setName("int", source, "_place_int_name", "int_name")
                .setName("loc", source, "_place_loc_name", "loc_name")
                .setName("old", source, "_place_old_name", "old_name")
                .setName("reg", source, "_place_reg_name", "reg_name")
                .setName("housename", source,"addr:housename");
    }

    NameMap setLocaleNames(Map<String, String> source, String[] languages) {
        setName("default", source, "_place_name", "name");
        for (var lang : languages) {
            setName(lang, source, "_place_name:" + lang, "name:" + lang);
        }
        return this;
    }

    NameMap setName(String field, Map<String, String> source, String... keys) {
        if (!containsKey(field)) {
            Arrays.stream(keys)
                    .map(source::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(k -> entries.add(new SimpleImmutableEntry<>(field, k)));
        }
        return this;
    }
}
