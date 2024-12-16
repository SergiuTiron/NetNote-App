import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import commons.Collection;
import server.database.CollectionRepository;
import server.service.CollectionService;

import java.util.Optional;

public class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private CollectionService collectionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testNoDuplicateDefaultCollection() {
        Collection existingCollection = new Collection("Default Collection");
        when(collectionRepository.findByName("Default Collection")).thenReturn(Optional.of(existingCollection));
        Collection defaultCollection = collectionService.getOrCreateDefaultCollection();
        assertNotNull(defaultCollection, "Returned collection should not be null");
        assertEquals(existingCollection, defaultCollection, "Should return the existing Default Collection");
        verify(collectionRepository, never()).save(any(Collection.class));
    }
}