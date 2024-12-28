import commons.Note;
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

    @Test
    void deleteCollectionByID_Exists() {
        // Test whether it deletes the collection when it exists
        long collectionId = 1L;
        when(collectionRepository.existsById(collectionId)).thenReturn(true);
        collectionService.deleteCollectionByID(collectionId);
        verify(collectionRepository, times(1)).deleteById(collectionId);
    }

    @Test
    void deleteCollectionByID_DoesNotExist() {
        // Test whether it throws an exception when the collection does not exist
        long collectionId = 1L;
        when(collectionRepository.existsById(collectionId)).thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            collectionService.deleteCollectionByID(collectionId));

        assertEquals("Collection with ID" + collectionId + " does not exist", exception.getMessage());
    }

    @Test
    void addNoteToCollection_CollectionExists() {
        // Should add the note when collection exists
        long collectionId = 1L;
        Note note = new Note("some note");
        Collection collection = new Collection("some collection");
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        when(collectionRepository.save(collection)).thenReturn(collection);

        Collection updatedCollection = collectionService.addNoteToCollection(collectionId, note);

        verify(collectionRepository, times(1)).save(collection);
        assertTrue(updatedCollection.getNotes().contains(note));
    }

    @Test
    void addNoteToCollection_CollectionDoesNotExist() {
        // Should add the note when collection does not exist
        long collectionId = 1L;
        Note note = new Note("some note");
        when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
            collectionService.addNoteToCollection(collectionId, note));

        assertEquals("Collection not found", exception.getMessage());
    }

    @Test
    void getOrCreateDefaultCollection_Get() {
        // Mock findByName to return empty Optional
        when(collectionRepository.findByName("Default Collection")).thenReturn(Optional.empty());

        Collection collection = new Collection("Default Collection");
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        Collection result = collectionService.getOrCreateDefaultCollection();

        verify(collectionRepository, times(1)).findByName("Default Collection");
        verify(collectionRepository, times(1)).save(any(Collection.class));

        assertNotNull(result);
        assertEquals("Default Collection", result.getName());
    }
}