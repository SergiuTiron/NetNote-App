package server.service;

import commons.ApplicationPreference;
import org.springframework.stereotype.Service;
import server.database.ApplicationPreferenceRepository;

@Service
public class PreferenceService {

    private final ApplicationPreferenceRepository repository;

    public PreferenceService(ApplicationPreferenceRepository repository) {
        this.repository = repository;
    }

    public void savePreference(String key, String value) {
        ApplicationPreference preference = repository.findByPreferenceKey(key);
        if(preference == null) {
            preference = new ApplicationPreference(key, value);
        }
        else {
            preference.setPreferenceValue(value);
        }
        repository.save(preference);
    }

    public String getPreference(String key) {
        ApplicationPreference preference = repository.findByPreferenceKey(key);
        return preference != null ? preference.getPreferenceValue() : null;
    }
}
