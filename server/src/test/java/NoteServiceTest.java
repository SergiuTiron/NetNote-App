import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.database.NoteRepository;
import server.service.NoteService;

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

	}

	@Test
	void deleteNoteById_DoesNotExist() {

	}

	@Test
	void updateNote_Exists() {

	}

	@Test
	void updateNote_DoesNotExist() {

	}

	@Test
	void updateNote_NullUpdated() {

	}
}