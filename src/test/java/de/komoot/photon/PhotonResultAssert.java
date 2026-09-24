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

    public PhotonResultAssert sameOsmID(int id) {
        isNotNull();

        if (!Integer.valueOf(id).equals(actual.get("osm_id"))) {
            failWithActualExpectedAndMessage(actual.get("osm_id"), id,
                    "Invalid OSM ID.");
        }

        return this;
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

    public PhotonResultAssert hasFieldValue(String field, Object expected) {
        Object value = actual.get(field);

        if (value == null) {
            failWithMessage("Field '%s' is missing.", field);
        }

        if (!value.equals(expected)) {
            failWithActualExpectedAndMessage(value, expected,
                    "Unexpected value for field '%s'.", field);
        }

        return this;
    }

    public PhotonResultAssert hasLocalisedFieldValue(String field, String locale, Object expected) {
        Object value = actual.getLocalised(field, locale);

        if (value == null) {
            failWithMessage("Field '%s' is missing.", field);
        }

        if (!value.equals(expected)) {
            failWithActualExpectedAndMessage(value, expected,
                    "Unexpected value for field '%s' (locale: %s).", field, locale);
        }

        return this;
    }

    public PhotonResultAssert hasNoField(String field) {
        if (actual.get(field) != null) {
            failWithMessage("Found field '%s' with content: %s", field, actual.get(field));
        }

        return this;
    }

    public PhotonResultAssert hasNoLocalisedField(String field, String locale) {
        if (actual.getLocalised(field, locale) != null) {
            failWithMessage("Found field '%s' with content: %s", field, actual.get(field));
        }

        return this;
    }
}
