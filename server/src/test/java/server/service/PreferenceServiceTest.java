package server.service;

import commons.ApplicationPreference;
import server.database.ApplicationPreferenceRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PreferenceServiceTest {

    private ApplicationPreferenceRepository repository;
    private PreferenceService preferenceService;

    @BeforeEach
    void setUp() {

        repository = Mockito.mock(ApplicationPreferenceRepository.class);
        preferenceService = new PreferenceService(repository);
    }

    @Test
    void testSavePreference_NewPreference() {

        String key = "lang";
        String value = "en";
        when(repository.findByPreferenceKey(key)).thenReturn(null);

        preferenceService.savePreference(key, value);
        //verify(repository, times(1)).findByPreferenceKey(key);
        verify(repository, times(1)).save(any(ApplicationPreference.class));
    }


    @Test
    void testSavePreference_UpdatePreference() {

        String key = "lang";
        String newValue = "en";
        String oldValue = "ro";

        ApplicationPreference existingPreference = new ApplicationPreference(key, oldValue);
        when(repository.findByPreferenceKey(key)).thenReturn(existingPreference);

        preferenceService.savePreference(key, newValue);

        assertEquals(newValue, existingPreference.getPreferenceValue());
        verify(repository, times(1)).save(existingPreference);

    }


    @Test
    void testGetPreference() {

        String key = "lang";
        String value = "en";

        ApplicationPreference preference = new ApplicationPreference(key, value);
        when(repository.findByPreferenceKey(key)).thenReturn(preference);

        String result = preferenceService.getPreference(key);
        assertEquals(value, result);
        verify(repository, times(1)).findByPreferenceKey(key);
    }

    @Test
    void testGetPreference_NotFound() {

        String key = "nonexistent";
        when(repository.findByPreferenceKey(key)).thenReturn(null);

        String result = preferenceService.getPreference(key);
        assertNull(result);
        verify(repository, times(1)).findByPreferenceKey(key);
    }
}