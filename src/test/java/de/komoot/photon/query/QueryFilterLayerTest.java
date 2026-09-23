package de.komoot.photon.query;

import de.komoot.photon.Importer;
import de.komoot.photon.nominatim.model.AddressType;
import de.komoot.photon.searcher.PhotonResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryFilterLayerTest extends BaseTesterQuery {
    @BeforeAll
    void setUp(@TempDir Path dataDirectory) throws Exception {
        setUpES(dataDirectory);
        Importer instance = makeImporter();

        AddressType[] docRanks = {AddressType.STATE, AddressType.CITY, AddressType.CITY, AddressType.LOCALITY};
        for (var rank : docRanks) {
            instance.add(List.of(createDoc("name", "berlin")
                    .centroid(makePoint(10, 10))
                    .addressType(rank)));
        }

        instance.finish();
        refresh();
    }

    @AfterAll
    @Override
    public void tearDown() {
        super.tearDown();
    }

    private List<PhotonResult> searchWithLayers(String... layers) {
        var request = new SimpleSearchRequest();
        request.setQuery("berlin");
        request.addLayerFilters(Arrays.stream(layers).collect(Collectors.toSet()));

        return search(request);
    }

    private List<PhotonResult> reverse(String... layers) {
        ReverseRequest request = new ReverseRequest(FACTORY.createPoint(new Coordinate(10, 10)));
        request.setLimit(15, 15);
        request.addLayerFilters(Arrays.stream(layers).collect(Collectors.toSet()));

        return reverse(request);
    }

    @Test
    void testSearchSingleLayer() {
        assertThat(searchWithLayers("city"))
                .extracting(p -> p.getOrDefault("type", ""))
                .containsExactly("city", "city");
    }

    @Test
    void testSearchMultipleLayers() {
        assertThat(searchWithLayers("city", "locality"))
                .extracting(p -> p.getOrDefault("type", ""))
                .containsExactlyInAnyOrder("city", "city", "locality");
    }

    @Test
    void testReverseSingleLayer() {
        assertThat(reverse("city"))
                .extracting(p -> p.getOrDefault("type", ""))
                .containsExactly("city", "city");
    }

    @Test
    void testReverseMultipleLayers() {
        assertThat(reverse("city", "locality"))
                .extracting(p -> p.getOrDefault("type", ""))
                .containsExactlyInAnyOrder("city", "city", "locality");
    }
}
