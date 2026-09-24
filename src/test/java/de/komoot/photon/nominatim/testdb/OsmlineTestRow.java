package de.komoot.photon.nominatim.testdb;

import de.komoot.photon.PhotonDoc;
import de.komoot.photon.nominatim.model.AddressType;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.*;

public class OsmlineTestRow {
    private static long placeIdSequence = 100000;

    private final Long placeId;
    private Long parentPlaceId;
    private final Long osmId = 23L;
    private Integer startnumber;
    private Integer endnumber;
    private Integer step;
    private String lineGeo;

    public OsmlineTestRow() {
        placeId = placeIdSequence++;
        lineGeo = "LINESTRING(0 0, 0.1 0.1, 0 0.2)";
    }

    public OsmlineTestRow number(int start, int end, int step) {
        startnumber = start;
        endnumber = end;
        this.step = step;
        return this;
    }

    public OsmlineTestRow parent(PlacexTestRow row) {
        parentPlaceId = row.getPlaceId();
        return this;
    }

    public OsmlineTestRow geom(String geom) {
        lineGeo = geom;
        return this;
    }

    public OsmlineTestRow add(JdbcTemplate jdbc) {
        jdbc.update("INSERT INTO location_property_osmline (place_id, parent_place_id, osm_id,"
                        + " startnumber, endnumber, step, linegeo, country_code, indexed_status)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, 'de', 0)",
                placeId, parentPlaceId, osmId, startnumber, endnumber, step, lineGeo);

        return this;
    }

    public void assertEquals(PhotonDoc doc) {
        assertThat(doc)
                .hasFieldOrPropertyWithValue("osmType", "W")
                .hasFieldOrPropertyWithValue("osmId", osmId)
                .hasFieldOrPropertyWithValue("tagKey", "place")
                .hasFieldOrPropertyWithValue("tagValue", "house_number")
                .hasFieldOrPropertyWithValue("countryCode", "DE")
                .hasFieldOrPropertyWithValue("addressType", AddressType.HOUSE);
    }

    public Long getPlaceId() {
        return this.placeId;
    }

    public String getPlaceString() {
        return Long.toString(this.placeId);
    }
}
