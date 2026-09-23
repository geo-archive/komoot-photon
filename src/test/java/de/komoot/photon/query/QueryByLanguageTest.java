package de.komoot.photon.query;

import de.komoot.photon.nominatim.model.AddressType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tests for queries in different languages.
 */
class QueryByLanguageTest extends BaseTesterQuery {
    @TempDir
    private Path dataDirectory;

    private void setup(String... languages) throws IOException {
        getProperties().setLanguages(Arrays.stream(languages).collect(Collectors.toSet()));
        setUpES(dataDirectory);
    }

    @Test
    void queryNonStandardLanguages() throws IOException {
        setup("en", "fi");
        setupDocs(createDoc("name", "original", "name:fi", "finish", "name:ru", "russian"));

        assertThat(search("original", "en")).hasSize(1);
        assertThat(search("finish", "en")).hasSize(1);
        assertThat(search("russian", "en")).hasSize(0);
    }

    @Test
    void queryAltNames() throws IOException {
        setup("de");
        setupDocs(createDoc("name", "simple", "alt_name", "ancient", "name:de", "einfach"));

        assertThat(search("simple", "de")).hasSize(1);
        assertThat(search("einfach", "de")).hasSize(1);
        assertThat(search("ancient", "de")).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(names = {"STREET", "LOCALITY", "DISTRICT", "CITY", "COUNTRY", "STATE"})
    void queryAddressPartsLanguages(AddressType addressType) throws IOException {
        setup("en", "de");

        var doc = createDoc("name", "here").tagKey("place").tagValue("house");

        doc.setAddressPartIfNew(addressType, makeAddressNames(
                "name", "original",
                "name:de", "deutsch"));

        setupDocs(doc);

        assertThat(search("here, original", "de")).hasSize(1);
        assertThat(search("here, Deutsch", "de")).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"default", "de", "en"})
    void queryAltNamesFuzzy(String lang) throws IOException {
        setup("de", "en");
        setupDocs(createDoc("name", "simple", "alt_name", "ancient", "name:de", "einfach"));

        assertThat(search("simplle", lang)).hasSize(1);
        assertThat(search("einfah", lang)).hasSize(1);
        assertThat(search("anciemt", lang)).hasSize(1);
        assertThat(search("sinister", lang)).hasSize(0);
    }
}
