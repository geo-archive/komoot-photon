package de.komoot.photon;

import de.komoot.photon.opensearch.DocFields;
import de.komoot.photon.searcher.PhotonResult;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.InstanceOfAssertFactory;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class PhotonResultAssert extends AbstractAssert<PhotonResultAssert, PhotonResult> {
    public static InstanceOfAssertFactory<PhotonResult, PhotonResultAssert> PHOTONRESULT
            = new InstanceOfAssertFactory<>(PhotonResult.class, PhotonResultAssert::new);

    protected PhotonResultAssert(PhotonResult result) {
        super(result, PhotonResultAssert.class);
    }

    public static PhotonResultAssert assertThat(PhotonResult result) {
        return new PhotonResultAssert(result);
    }

    public static PhotonResultAssert assertThat(List<PhotonResult> results, int idx) {
        return new PhotonResultAssert((results != null && idx < results.size()) ? results.get(idx) : null);
    }

    public PhotonResultAssert sameOsmID(PhotonDoc doc) {
        if (actual == null) {
            failWithMessage("Element not found in list.");
        }

        var osmType = actual.get("osm_type");
        var osmId = actual.get("osm_id");

        if (osmType == null || osmId == null) {
            failWithMessage("Invalid OSM ID in result.");
        }

        if (doc.getOsmType() == null) {
            failWithMessage("Photon document not comparable by ID.");
        }

        long longOsmId;
        if (osmId instanceof Integer) {
            longOsmId = (Integer) osmId;
        } else {
            longOsmId = (Long) osmId;
        }

        if (!osmType.equals(doc.getOsmType()) || longOsmId != doc.getOsmId()) {
            failWithActualExpectedAndMessage(
                    osmType + osmId.toString(),
                    doc.getOsmType() + doc.getOsmId(),
                    "Unexpected OSM ID."
            );
        }

        return this;
    }

    public PhotonResultAssert hasGeometryType(String gtype) {
        assertThatJson(actual.get(DocFields.GEOMETRY))
                .isObject().containsEntry("type", gtype);

        return this;
    }
}
