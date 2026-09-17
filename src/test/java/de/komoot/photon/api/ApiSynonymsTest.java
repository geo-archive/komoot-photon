package de.komoot.photon.api;

import de.komoot.photon.App;
import de.komoot.photon.Importer;
import de.komoot.photon.PhotonDoc;
import de.komoot.photon.nominatim.model.AddressType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiSynonymsTest extends ApiBaseTester {

    private static final Date TEST_DATE = new Date();

    @BeforeAll
    void setUp(@TempDir Path dataDirectory) throws Exception {
        getProperties().setSupportGeometries(true);
        getProperties().setExtraTags(List.of("ALL"));
        getProperties().setImportDate(TEST_DATE);
        setUpES(dataDirectory);
        Importer instance = makeImporter();
        instance.add(List.of(new PhotonDoc()
                .placeId("1000").osmType("N").osmId(1000).tagKey("place").tagValue("city")
                .categories(List.of("osm.place.city"))
                .importance(0.6).addressType(AddressType.CITY)
                .centroid(makePoint(13.38886, 52.51704))
                .geometry(makeDocGeometry("POINT(13.38886 52.51704)"))
                .names(makeDocNames("name", "berlin"))
        ));

        instance.finish();
        refresh();
    }

    @AfterAll
    @Override
    public void tearDown() {
        shutdownES();
    }

    private List<String> cfgSynonyms(String... synonyms) {
        var lines = new ArrayList<String>();
        lines.add("{ \"search_synonyms\": [");
        lines.addAll(Arrays.stream(synonyms).map(s -> '"' + s + '"').toList());
        lines.add("]}");

        return lines;
    }

    @Test
    void testSimpleSynonym(@TempDir Path dataDir) throws Exception {
        var synonymFile = dataDir.resolve("synonyms.json");
        Files.write(synonymFile, cfgSynonyms("berlin,hauptstadt"));

        startAPI("-synonym-file", synonymFile.toString());

        assertThatJson(readURL("/api?q=hauptstadt")).isObject()
                .node("features").isArray().hasSize(1);

        App.shutdown();
    }

    @Test
    void testDisallowSpacesInSynonyms(@TempDir Path dataDir) throws Exception {
        var synonymFile = dataDir.resolve("synonyms.json");
        Files.write(synonymFile, cfgSynonyms("berlin,haupt stadt"));

        assertThatException()
                .isThrownBy(() -> startAPI("-synonym-file", synonymFile.toString()))
                .withMessageContaining("Terms must not contain spaces");
    }
}
