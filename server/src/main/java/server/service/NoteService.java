package server.service;

import commons.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.NoteRepository;

import java.util.List;
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
        if(updatedNote == null)
            throw new IllegalArgumentException("Note with ID" + id + " does not exist");
        if (retrievedNote.isPresent()) {
            Note noteToSave = retrievedNote.get();
            noteToSave.setTitle(updatedNote.getTitle());
            return noteRepository.save(updatedNote);
        }
        else
            throw new RuntimeException();
    }

    /**
     * method that triggers the search of the keyword in the note repository
     * @param keyword
     * @param collectionId the id of the currently selected collection
     * @return the list of notes containing the keyword
     */
    public List<Note> searchKeyword(String keyword, Long collectionId) {
        if (collectionId == null) {
            return noteRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
        }
        return noteRepository.findByCollectionIdAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                collectionId, keyword, keyword
        );
    }



}
