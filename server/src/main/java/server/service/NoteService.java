package server.service;

import commons.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.NoteRepository;

import java.util.Optional;

@Service
public class NoteService {

    private NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public void deleteNoteById(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new IllegalArgumentException("Note with ID" + id + " does not exist");
        }
        noteRepository.deleteById(id);
    }

    // method that updates the note both locally and in the repository
    public Note updateNote(Long id, Note updatedNote) throws RuntimeException {
        Optional<Note> retrievedNote = noteRepository.findById(id);
        if (retrievedNote.isEmpty()) {
            throw new IllegalArgumentException("Note with ID " + id + " does not exist");
        }
        if(updatedNote == null)
            throw new IllegalArgumentException("There is no update to note (updateNote is null).");

	    Note noteToSave = retrievedNote.get();
	    noteToSave.setTitle(updatedNote.getTitle());
	    return noteRepository.save(updatedNote);
    }

}
