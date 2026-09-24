package de.komoot.photon.opensearch;

import de.komoot.photon.ESBaseTester;
import de.komoot.photon.Importer;
import de.komoot.photon.PhotonDoc;
import de.komoot.photon.Updater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static de.komoot.photon.PhotonResultAssert.*;

class UpdaterTest extends ESBaseTester {

    @BeforeEach
    public void setUp(@TempDir Path dataDirectory) throws IOException {
        getProperties().setSupportGeometries(true);
        setUpES(dataDirectory);
    }

    private PhotonDoc createDoc(String... names) {
        return new PhotonDoc()
                .placeId("1234").osmType("N").osmId(1000).tagKey("place").tagValue("city")
                .names(makeDocNames(names));
    }

    @Test
    void addNameToDoc() {
        PhotonDoc doc = createDoc("name", "Foo");

        setupDocs(doc);

        assertThat(getById(1234)).isNotNull()
                .hasLocalisedFieldValue(DocFields.NAME, "en", "Foo")
                .hasLocalisedFieldValue(DocFields.NAME, "default", "Foo");

        doc.names(makeDocNames("name", "Foo", "name:en", "Enfoo"));
        updateDocs(doc);

        assertThat(getById(1234)).isNotNull()
                .hasLocalisedFieldValue(DocFields.NAME, "en", "Enfoo")
                .hasLocalisedFieldValue(DocFields.NAME, "default", "Foo");
    }

    @Test
    void removeNameFromDoc() {
        PhotonDoc doc = createDoc("name", "Foo", "name:en", "Enfoo");

        setupDocs(doc);

        assertThat(getById(1234)).isNotNull()
                .hasLocalisedFieldValue(DocFields.NAME, "en", "Enfoo")
                .hasLocalisedFieldValue(DocFields.NAME, "default", "Foo");

        doc.names(makeDocNames("name:en", "Enfoo"));
        updateDocs(doc);

        assertThat(getById(1234)).isNotNull()
                .hasLocalisedFieldValue(DocFields.NAME, "en", "Enfoo")
                .hasNoLocalisedField(DocFields.NAME, "default");
    }

    @Test
    void addExtraTagsToDoc() {
        getProperties().setExtraTags(List.of("website"));

        PhotonDoc doc = createDoc("name", "Foo");

        setupDocs(doc);

        assertThat(getById(1234)).isNotNull()
                        .hasNoField(DocFields.EXTRA);

        doc.extraTags(Map.of("website", "http://site.foo"));
        updateDocs(doc);

        assertThat(getById(1234)).isNotNull()
                .hasFieldValue(DocFields.EXTRA, Map.of("website", "http://site.foo"));
    }

    @Test
    void deleteDoc() {
        Importer instance = makeImporter();
        instance.add(List.of(
                createDoc().houseNumber("34"),
                createDoc().houseNumber("35")));
        instance.finish();
        refresh();

        assertThat(getById("1234")).isNotNull();
        assertThat(getById("1234.1")).isNotNull();

        Updater updater = makeUpdater();
        updater.delete("1234");
        updater.finish();
        refresh();

        assertThat(getById("1234")).isNotNull();
        assertThat(getById("1234.1")).isNull();
    }
}