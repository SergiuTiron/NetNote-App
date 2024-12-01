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

    public void deleteNoteById(Long Id) {
        if (!noteRepository.existsById(Id)) {
            throw new IllegalArgumentException("Note with ID" + Id + " does not exist");
        }
        noteRepository.deleteById(Id);
    }

    // method that updates the note both locally and in the repository
    public Note updateNote(Long id, Note updatedNote) throws RuntimeException {
        Optional<Note> retrievedNote = noteRepository.findById(id);
        if (retrievedNote.isPresent()) {
            Note noteToSave = retrievedNote.get();
            noteToSave.setTitle(updatedNote.getTitle());
            Note savedNote = noteRepository.save(updatedNote);
            return savedNote;
        }
        else
            throw new RuntimeException();
    }

}
