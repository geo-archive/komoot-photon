package de.komoot.photon.query;

import de.komoot.photon.PhotonDoc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static de.komoot.photon.PhotonResultAssert.assertThat;

class QueryGeometryTest extends BaseTesterQuery {

    @BeforeEach
    void setup(@TempDir Path dataDirectory) throws IOException {
        getProperties().setSupportGeometries(true);
        setUpES(dataDirectory);
    }

    private PhotonDoc createDoc(String geometry) {
        return createDoc("name", "Muffle Flu")
                .geometry(makeDocGeometry(geometry))
                .centroid(makePoint(1.0, 2.34));
    }

    @Test
    void testSearchGetPolygon()  {
        setupDocs(createDoc("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))"));

        assertThat(search("muffle flu"), 0).hasGeometryType("Polygon");
    }

    @Test
    void testSearchGetLineString()  {
        setupDocs(createDoc("LINESTRING (30 10, 10 30, 40 40)"));

        assertThat(search("muffle flu"), 0).hasGeometryType("LineString");
    }
}
