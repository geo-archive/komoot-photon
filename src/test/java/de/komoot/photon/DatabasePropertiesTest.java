package de.komoot.photon;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the database-global property store.
 */
class DatabasePropertiesTest {

    /**
     * setLanguages() overwrites the language settings.
     */
    @Test
    void testSetLanguages() {
        var now = new Date();
        DatabaseProperties prop = new DatabaseProperties();
        prop.setLanguages(Set.of("en", "bg", "de"));
        prop.setImportDate(now);

        assertThat(prop.getLanguages()).containsExactlyInAnyOrder("en", "bg", "de");
        assertThat(prop.getImportDate()).hasSameTimeAs(now);
    }
}
