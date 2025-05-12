package de.komoot.photon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

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
        prop.setLanguages(new String[]{"en", "bg", "de"});
        prop.setImportDate(now);

        assertArrayEquals(new String[]{"en", "bg", "de"}, prop.getLanguages());
        assertEquals(now, prop.getImportDate());
    }

    /**
     * If languages is not set, then the restricted language set is used as is.
     */
    @Test
    void testRestrictLanguagesUnsetLanguages() {
        DatabaseProperties prop = new DatabaseProperties();
        prop.restrictLanguages(new String[]{"en", "bg", "de"});

        assertArrayEquals(new String[]{"en", "bg", "de"}, prop.getLanguages());
    }

    /**
     * When languages are set, then only the languages of the restricted set are used
     * that already exist and the order of the input is preserved.
     */
    @Test
    void testRestrictLanguagesAlreadySet() {
        DatabaseProperties prop = new DatabaseProperties();
        prop.setLanguages(new String[]{"en", "de", "fr"});

        prop.restrictLanguages(new String[]{"cn", "de", "en", "es"});

        assertArrayEquals(new String[]{"de", "en"}, prop.getLanguages());
    }

}
