package server.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import server.service.PreferenceService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class PreferenceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PreferenceService preferenceService;

    @InjectMocks
    private PreferenceController preferenceController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(preferenceController).build();
    }

    @Test
    public void testSavePreference() throws Exception {
        String key = "language";
        String value = "english";

        doNothing().when(preferenceService).savePreference(key, value);

        mockMvc.perform(post("/api/preferences")
                        .param("key", key)
                        .param("value", value)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(preferenceService, times(1)).savePreference(key, value);
    }

    @Test
    public void testGetPreference() throws Exception {
        String key = "language";
        String expectedValue = "english";

        when(preferenceService.getPreference(key)).thenReturn(expectedValue);

        mockMvc.perform(get("/api/preferences")
                        .param("key", key))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedValue));

        verify(preferenceService, times(1)).getPreference(key);
    }
}
