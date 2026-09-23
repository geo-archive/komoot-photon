package de.komoot.photon.query;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static de.komoot.photon.PhotonResultAssert.*;

public class ConfigureNormalizationTest extends BaseTesterQuery {

    @Test
    void testDefaultNormalization(@TempDir Path dataDirectory) throws IOException {
        setUpES(dataDirectory);
        setupDocs(
                createDoc("name", "RO32").osmId(100),
                createDoc("name", "München").osmId(200)
        );

        assertThat(search("RO32"), 0).sameOsmID(100);
        assertThat(search("ro32"), 0).sameOsmID(100);
        assertThat(search("munchen"), 0).sameOsmID(200);
        assertThat(search("muenchen"), 0).sameOsmID(200);
    }

    @Test
    void testDisableAllNormalization(@TempDir Path dataDirectory) throws IOException {
        setUpES(dataDirectory, List.of());
        setupDocs(
                createDoc("name", "RO32").osmId(100),
                createDoc("name", "München").osmId(200)
        );

        assertThat(search("RO32"), 0).sameOsmID(100);
        assertThat(search("ro32")).isEmpty();
        assertThat(search("München"), 0).sameOsmID(200);
        assertThat(search("munchen")).isEmpty();
        assertThat(search("muenchen")).isEmpty();
    }

    @Test
    void testCustomNormalization(@TempDir Path dataDirectory) throws IOException {
        setUpES(dataDirectory, List.of("apostrophe"));
        setupDocs(
                createDoc("name", "John's").osmId(100),
                createDoc("name", "peter").osmId(200)
        );

        assertThat(search("John"), 0).sameOsmID(100);
        assertThat(search("John's"), 0).sameOsmID(100);
        assertThat(search("john's")).isEmpty();
        assertThat(search("peter's"), 0).sameOsmID(200);
        assertThat(search("peter"), 0).sameOsmID(200);
    }
}
