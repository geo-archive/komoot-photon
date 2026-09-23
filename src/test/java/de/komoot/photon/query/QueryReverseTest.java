package de.komoot.photon.query;

import de.komoot.photon.PhotonDoc;
import org.locationtech.jts.geom.Coordinate;
import de.komoot.photon.searcher.PhotonResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static de.komoot.photon.PhotonResultAssert.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryReverseTest extends BaseTesterQuery {

    private PhotonDoc createDoc(int id, double x, double y) {
        return createDoc("name", "some house").osmId(id).centroid(makePoint(x, y));
    }

    @BeforeAll
    void setup(@TempDir Path dataDirectory) throws IOException {
        setUpES(dataDirectory);

        setupDocs(createDoc(100, 10, 10),
                createDoc(101, 10, 10.1),
                createDoc(102, 10, 10.2),
                createDoc(103, -10, -10));
    }

    @AfterAll
    @Override
    public void tearDown() {
        super.tearDown();
    }

    private List<PhotonResult> reverse(double lon, double lat, double radius, Integer limit) {
        final var request = new ReverseRequest(FACTORY.createPoint(new Coordinate(lon, lat)));
        request.setRadius(radius);
        if (limit != null) {
            request.setLimit(limit, limit);
        }

        return reverse(request);
    }

    @Test
    void testReverse() {
        assertThat(reverse(10, 10, 0.1, 1))
                .singleElement(PHOTONRESULT).sameOsmID(100);
    }

    @Test
    void testDefaultLimitIsOne() {
        assertThat(reverse(10, 10, 20, null))
                .singleElement(PHOTONRESULT).sameOsmID(100);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10})
    void testReverseMultiple(int limit) {
        assertThat(reverse(10, 10, 20, limit))
                .satisfiesExactly(
                        p -> assertThat(p).sameOsmID(100),
                        p -> assertThat(p).sameOsmID(101)
                );
    }
}
