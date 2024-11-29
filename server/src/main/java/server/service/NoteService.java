package server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.NoteRepository;

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
}
