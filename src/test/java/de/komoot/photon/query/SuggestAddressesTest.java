package de.komoot.photon.query;

import de.komoot.photon.PhotonDoc;
import de.komoot.photon.nominatim.model.AddressType;
import de.komoot.photon.searcher.PhotonResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static de.komoot.photon.PhotonResultAssert.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SuggestAddressesTest extends BaseTesterQuery {

    private static final String STREET_NAME = "Test Street";

    // Add a street
    private final PhotonDoc street = new PhotonDoc("1", "W", 1, "highway", "residential")
            .names(makeDocNames("name", STREET_NAME))
            .countryCode("DE")
            .importance(0.5)
            .addressType(AddressType.STREET);

    // Add a house on that street
    private final PhotonDoc house = new PhotonDoc("2", "N", 2, "building", "yes")
            .countryCode("DE")
            .houseNumber("42")
            .addAddresses(Map.of("street", STREET_NAME), getProperties().getLanguages())
            .importance(0.1)
            .addressType(AddressType.HOUSE);

    // Add houses on same street name in different cities (Auelestr scenario)
    private final PhotonDoc houseTriesen = new PhotonDoc("3", "N", 3, "building", "yes")
            .countryCode("LI")
            .houseNumber("16")
            .addAddresses(Map.of("street", "Auelestr", "city", "Triesen"), getProperties().getLanguages())
            .importance(0.1)
            .addressType(AddressType.HOUSE);

    private final PhotonDoc houseVaduz = new PhotonDoc("4", "N", 4, "building", "yes")
            .countryCode("LI")
            .houseNumber("16")
            .addAddresses(Map.of("street", "Auelestr", "city", "Vaduz"), getProperties().getLanguages())
            .importance(0.1)
            .addressType(AddressType.HOUSE);

    // Add a street with pure alphabetic name (triggers short query path)
    private final PhotonDoc alphabeticStreet = new PhotonDoc("5", "W", 5, "highway", "residential")
            .names(makeDocNames("name", "Romsdalsveien"))
            .countryCode("NO")
            .importance(0.5)
            .addressType(AddressType.STREET);

    // Add a house on that street
    private final PhotonDoc houseRomsdalsveien = new PhotonDoc("6", "N", 6, "building", "yes")
            .countryCode("NO")
            .houseNumber("10")
            .addAddresses(Map.of("street", "Romsdalsveien"), getProperties().getLanguages())
            .importance(0.1)
            .addressType(AddressType.HOUSE);

    // Add a street with spaces in name (triggers full query path)
    private final PhotonDoc multiWordStreet = new PhotonDoc("7", "W", 7, "highway", "residential")
            .names(makeDocNames("name", "Nils Gotlands veg"))
            .countryCode("NO")
            .importance(0.5)
            .addressType(AddressType.STREET);

    // Add a house on that street
    private final PhotonDoc houseNilsGotlands = new PhotonDoc("8", "N", 8, "building", "yes")
            .countryCode("NO")
            .houseNumber("5")
            .addAddresses(Map.of("street", "Nils Gotlands veg"), getProperties().getLanguages())
            .importance(0.1)
            .addressType(AddressType.HOUSE);


    @BeforeAll
    void setUp(@TempDir Path dataDirectory) throws Exception {
        setUpES(dataDirectory);

        setupDocs(street, house, houseTriesen, houseVaduz, alphabeticStreet, houseRomsdalsveien,
                multiWordStreet, houseNilsGotlands);
    }

    @AfterAll
    @Override
    public void tearDown() {
        super.tearDown();
    }

    private List<PhotonResult> searchWithSuggestAddresses(String query) {
        var request = new SimpleSearchRequest();
        request.setQuery(query);
        request.setSuggestAddresses(true);

        return search(request);
    }

    @Test
    void searchWithoutSuggestAddressesReturnsOnlyStreet() {
        assertThat(search(STREET_NAME))
                .hasSize(1)
                .first(PHOTONRESULT).sameOsmID(street);
    }

    @Test
    void searchWithSuggestAddressesReturnsAddresses() {
        assertThat(searchWithSuggestAddresses(STREET_NAME))
                .satisfiesExactly(
                        p -> assertThat(p).sameOsmID(street),
                        p -> assertThat(p).sameOsmID(house)
                );
    }

    @Test
    void searchWithHousenumberInQueryDoesNotTriggerSuggestAddresses() {
        // When query already contains a number, suggest_addresses should not add
        // the alternative housenumber query path (it's redundant since the main query
        // already handles housenumber matching)
        assertThat(searchWithSuggestAddresses(STREET_NAME + " 42"))
                .hasSize(1)
                .first(PHOTONRESULT).sameOsmID(house);
    }

    @Test
    void suggestAddressesRespectsOtherQueryTerms() {
        // When searching for "Auelestr Triesen", should only return addresses in Triesen,
        // not addresses in Vaduz just because they have a housenumber on the same street name
        assertThat(searchWithSuggestAddresses("Auelestr Triesen"))
                .hasSize(1)
                .first(PHOTONRESULT).sameOsmID(houseTriesen);
    }

    @Test
    void suggestAddressesWorksForPureAlphabeticStreetNames() {
        // Pure alphabetic queries (like "Romsdalsveien") use the short query path.
        // suggest_addresses should work for these too.
        assertThat(searchWithSuggestAddresses("Romsdalsveien"))
                .satisfiesExactly(
                        p -> assertThat(p).sameOsmID(alphabeticStreet),
                        p -> assertThat(p).sameOsmID(houseRomsdalsveien)
                );
    }

    @Test
    void suggestAddressesWorksForMultiWordStreetNames() {
        // Multi-word queries (like "Nils Gotlands veg") use the full query path.
        assertThat(searchWithSuggestAddresses("Nils Gotlands veg"))
                .satisfiesExactly(
                        p -> assertThat(p).sameOsmID(multiWordStreet),
                        p -> assertThat(p).sameOsmID(houseNilsGotlands)
                );
    }
}
