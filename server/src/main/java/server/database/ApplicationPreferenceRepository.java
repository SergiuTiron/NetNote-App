package server.database;

import commons.ApplicationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationPreferenceRepository extends JpaRepository<ApplicationPreference, Integer> {
    ApplicationPreference findByPreferenceKey(String preferenceKey);
}
