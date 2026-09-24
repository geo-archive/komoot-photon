package de.komoot.photon;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import static org.assertj.core.api.Assertions.*;

class PhotonDocInterpolationSetTest {
    private final PhotonDoc baseDoc = new PhotonDoc("10000", "N", 123, "place", "house")
            .countryCode("de");
    private final WKTReader reader = new WKTReader();
    private Geometry lineGeo;

    private void assertDocWithHousenumber(PhotonDoc doc, String housenumber, double y) {
        assertThat(doc)
                .hasFieldOrPropertyWithValue("houseNumber", housenumber)
                .satisfies(d -> assertThat(d.getCentroid().getX()).isCloseTo(2.5, Percentage.withPercentage(0.001)))
                .satisfies(d -> assertThat(d.getCentroid().getY()).isCloseTo(y, Percentage.withPercentage(0.001)))
                .usingRecursiveComparison()
                .ignoringFields("houseNumber", "centroid")
                .isEqualTo(baseDoc);
    }

    @BeforeEach
    void setupGeometry() throws ParseException {
        lineGeo = reader.read("LINESTRING(2.5 0.0 ,2.5 0.1)");
    }

    @Test
    void testBadInterpolationReverse() {
        assertThat(new PhotonDocInterpolationSet(baseDoc, 34, 33, 1, lineGeo))
                .isEmpty();
    }

    @Test
    void testBadInterpolationLargeMulti()  {
        assertThat(new PhotonDocInterpolationSet(baseDoc, 1, 2000, 1, lineGeo))
                .isEmpty();
    }

    @Test
    void testSinglePointInterpolation() {
        assertThat(new PhotonDocInterpolationSet(baseDoc, 2000, 2000, 1, lineGeo))
                .singleElement()
                .satisfies(d -> assertDocWithHousenumber(d, "2000", 0.05));
    }

    @Test
    void testSingleStepInterpolation() {
        assertThat(new PhotonDocInterpolationSet(baseDoc, 1, 3, 1, lineGeo))
                .satisfiesExactly(
                        d1 -> assertDocWithHousenumber(d1, "1", 0),
                        d2 -> assertDocWithHousenumber(d2, "2", 0.05),
                        d3 -> assertDocWithHousenumber(d3, "3", 0.1));
    }

    @Test
    void testTwoStepInterpolation() {
        assertThat(new PhotonDocInterpolationSet(baseDoc, 16, 20, 2, lineGeo))
                .satisfiesExactly(
                        d1 -> assertDocWithHousenumber(d1, "16", 0),
                        d2 -> assertDocWithHousenumber(d2, "18", 0.05),
                        d3 -> assertDocWithHousenumber(d3, "20", 0.1));
    }
}