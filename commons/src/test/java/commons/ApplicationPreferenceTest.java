package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationPreferenceTest {

    @Test
    void testConstructor() {
        ApplicationPreference preference = new ApplicationPreference("language", "english");
        assertEquals("language", preference.getPreferenceKey());
        assertEquals("english", preference.getPreferenceValue());
        assertNotNull(preference.getPreferenceKey());
        assertNotNull(preference.getPreferenceValue());

        ApplicationPreference preferenceNull = new ApplicationPreference(null, null);
        assertNull(preferenceNull.getPreferenceKey());
        assertNull(preferenceNull.getPreferenceValue());
    }

    @Test
    void getId() {
        ApplicationPreference preference = new ApplicationPreference();
        preference.setId(1);
        assertEquals(1, preference.getId());
    }

    @Test
    void setId() {
        ApplicationPreference preference = new ApplicationPreference();
        preference.setId(1);
        preference.setId(2);
        assertEquals(2, preference.getId());
        assertNotEquals(1, preference.getId());
    }

    @Test
    void getPreferenceKey() {
        ApplicationPreference preference = new ApplicationPreference("language", null);
        assertEquals("language", preference.getPreferenceKey());
    }

    @Test
    void setPreferenceKey() {
        ApplicationPreference preference = new ApplicationPreference();
        preference.setPreferenceKey("language");
        assertEquals("language", preference.getPreferenceKey());
        assertNotEquals("theme", preference.getPreferenceKey());
    }

    @Test
    void getPreferenceValue() {
        ApplicationPreference preference = new ApplicationPreference(null, "french");
        assertEquals("french", preference.getPreferenceValue());
    }

    @Test
    void setPreferenceValue() {
        ApplicationPreference preference = new ApplicationPreference();
        preference.setPreferenceValue("chinese");
        assertEquals("chinese", preference.getPreferenceValue());
        assertNotEquals("japanese", preference.getPreferenceValue());
    }
}