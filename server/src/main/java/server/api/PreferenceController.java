package server.api;

import org.springframework.web.bind.annotation.*;
import server.service.PreferenceService;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {
    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @PostMapping
    public void savePreference(@RequestParam String key, @RequestParam String value) {
        preferenceService.savePreference(key, value);
    }

    @GetMapping
    public String getPreference(@RequestParam String key) {
        return preferenceService.getPreference(key);
    }
}
