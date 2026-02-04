package de.komoot.photon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ServerDatabasePropertiesTest extends ESBaseTester {

    @Test
    void testSaveAndLoadFromDatabase(@TempDir Path dataDirectory) throws IOException {
        setUpES(dataDirectory);

        final Date now = new Date();

        DatabaseProperties prop = new DatabaseProperties();
        prop.setLanguages(Set.of("en", "de", "fr"));
        prop.setImportDate(now);
        prop.setSupportGeometries(true);

        getServer().saveToDatabase(prop);

        prop = getServer().loadFromDatabase();

        assertThat(prop.getLanguages()).containsExactlyInAnyOrder("en", "de", "fr");
        assertThat(prop.getImportDate()).hasSameTimeAs(now);
        assertThat(prop.getSupportGeometries()).isTrue();
    }
}