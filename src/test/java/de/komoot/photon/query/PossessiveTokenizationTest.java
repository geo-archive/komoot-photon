package de.komoot.photon.query;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static de.komoot.photon.PhotonResultAssert.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PossessiveTokenizationTest extends BaseTesterQuery {

    @BeforeAll
    void setUp(@TempDir Path dataDirectory) throws IOException {
        setUpES(dataDirectory);

        setupDocs(
                createDoc("name", "Tiffany's").osmId(1),
                createDoc("name", "Lio's Cafe Bar").osmId(2),
                createDoc("name", "O'Connor").osmId(3),
                createDoc("name", "L'Etoile").osmId(4),
                createDoc("name", "Oslo S").osmId(5),
                createDoc("name", "L'Eglise").osmId(6),
                createDoc("name", "O'Reillys").osmId(7),
                createDoc("name", "Saint-Jean d'Acre").osmId(8),
                createDoc("name", "O' Sole Mio").osmId(9)
        );
    }

    @AfterAll
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    void indexNameNgramAnalyzerOutput() {
        assertThat(getTestServer().analyze("index_name_ngram", "lio's")).contains("lio", "lios").doesNotContain("s");
        assertThat(getTestServer().analyze("index_name_ngram", "o's")).contains("o", "os").doesNotContain("s");
        assertThat(getTestServer().analyze("index_name_ngram", "Côte d'Or")).contains("cote", "or", "dor");
        assertThat(getTestServer().analyze("index_name_ngram", "L'Étoile")).contains("letoile", "etoile").doesNotContain("l");
        assertThat(getTestServer().analyze("index_name_ngram", "L'Église")).contains("leglise", "eglise").doesNotContain("l");
        assertThat(getTestServer().analyze("index_name_ngram", "L'Été Bar")).contains("lete", "ete", "bar").doesNotContain("l");
        assertThat(getTestServer().analyze("index_name_ngram", "l'eglise")).contains("eglise", "leglise").doesNotContain("l");
        assertThat(getTestServer().analyze("index_name_ngram", "o'reillys")).contains("reillys", "oreillys").doesNotContain("o");
        assertThat(getTestServer().analyze("index_name_ngram", "MainStreet")).contains("main", "street", "mainstreet");
        assertThat(getTestServer().analyze("index_name_ngram", "d'acre")).contains("dacre", "acre").doesNotContain("d");
        assertThat(getTestServer().analyze("index_name_ngram", "d'Acre")).contains("acre", "dacre");
        assertThat(getTestServer().analyze("index_name_ngram", "Saint-Jean d'Acre")).contains("acre", "dacre");
    }

    @Test
    void osloSDoesNotPullInPossessivePois() {
        assertThat(search("Oslo S"))
                .singleElement(PHOTONRESULT).sameOsmID(5);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "'Tiffany',    1",
            "'Lio',        2",
            "'O''Connor',  3",
            "'Connor',     3",
            "'O Connor',   3",
            "'L''Etoile',  4",
            "'Etoile',     4",
            "'Acre',       8",
            "'Saint-Jean', 8",
            "'d''Acre',    8",
            "'o''so',      9",
            "'o''sol',     9",
            "'o'' sol',    9",
            "'o''sole',    9"
    })
    void queryFindsExpectedHit(String query, int expectedId) {
        assertThat(search(query)).singleElement(PHOTONRESULT).sameOsmID(expectedId);
    }

    @ParameterizedTest(name = "{0} must NOT surface {1}")
    @CsvSource({
            "'Tiffany',  2",
            "'Tiffany',  3",
            "'Connor',   1",
            "'Connor',   2"
    })
    void queryDoesNotReturnUnrelatedHit(String query, int forbiddenID) {
        assertThat(search(query))
                .noneSatisfy(p -> assertThat(p).sameOsmID(forbiddenID));
    }
}
