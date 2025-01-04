package commons;

import jakarta.persistence.*;

@Entity
@Table(name = "ApplicationPreference")
public class ApplicationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String preferenceKey;

    @Column(nullable = false)
    private String preferenceValue;

    public ApplicationPreference() {
    }

    public ApplicationPreference(String preferenceKey, String preferenceValue) {
        this.preferenceKey = preferenceKey;
        this.preferenceValue = preferenceValue;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPreferenceKey() {
        return this.preferenceKey;
    }

    public void setPreferenceKey(String preferenceKey) {
        this.preferenceKey = preferenceKey;
    }

    public String getPreferenceValue() {
        return this.preferenceValue;
    }

    public void setPreferenceValue(String preferenceValue) {
        this.preferenceValue = preferenceValue;
    }
}
