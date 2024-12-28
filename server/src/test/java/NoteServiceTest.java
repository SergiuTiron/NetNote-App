import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.database.NoteRepository;
import server.service.NoteService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class NoteServiceTest {

	@Mock
	private NoteRepository noteRepository;

	@InjectMocks
	private NoteService noteService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this); // Initialize mocks
	}

	@Test
	void deleteNoteById_Exists(){
		// Test to delete note by its id when it exists
		long noteId = 1L;

		when(noteRepository.existsById(noteId)).thenReturn(true);
		noteService.deleteNoteById(noteId);

		verify(noteRepository, times(1)).deleteById(noteId); // verify the method was called on the repository;
	}

	@Test
	void deleteNoteById_DoesNotExist() {
		// Test to throw exception when trying to delete note by its id when it does not exist
		long noteId = 1L;

		when(noteRepository.existsById(noteId)).thenReturn(false);
		Exception exception = assertThrows(IllegalArgumentException.class, () ->
			noteService.deleteNoteById(noteId));

		assertEquals("Note with ID" + noteId + " does not exist", exception.getMessage());
	}

	@Test
	void updateNote_Exists() {
		long noteId = 1L;
		Note existingNote = new Note();
		existingNote.setTitle("Old Title");

		Note updatedNote = new Note();
		updatedNote.setTitle("New Title");

		when(noteRepository.findById(noteId)).thenReturn(Optional.of(existingNote));
		when(noteRepository.save(updatedNote)).thenReturn(updatedNote);

		Note result = noteService.updateNote(noteId, updatedNote);

		verify(noteRepository, times(1)).findById(noteId);
		verify(noteRepository, times(1)).save(updatedNote);

		assertEquals("New Title", result.getTitle());
	}

	@Test
	void updateNote_DoesNotExist() {
		long noteId = 1L;
		Note updatedNote = new Note();

		when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

		Exception exception = assertThrows(RuntimeException.class, () ->
			noteService.updateNote(noteId, updatedNote));

		verify(noteRepository, times(1)).findById(noteId);
		verify(noteRepository, never()).save(any(Note.class)); // Ensure the note was never saved
	}

	@Test
	void updateNote_NullUpdated() {
		long noteId = 1L;
		Note exisitngNote = new Note();

		when(noteRepository.findById(noteId)).thenReturn(Optional.of(exisitngNote));

		Exception exception = assertThrows(IllegalArgumentException.class, () ->
			noteService.updateNote(noteId, null)
		);

		verify(noteRepository, times(1)).findById(noteId);
		verify(noteRepository, never()).save(any(Note.class)); // Ensure the note was never saved

		assertEquals("There is no update to note (updateNote is null).", exception.getMessage());
	}
}