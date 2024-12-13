package server.service;

import commons.Collection;
import commons.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.CollectionRepository;
import server.database.NoteRepository;

@Service
public class CollectionService {

    private CollectionRepository collectionRepository;
    private NoteRepository noteRepository;

    @Autowired
    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public void deleteCollectionByID(Long id) {
        if (!collectionRepository.existsById(id)) {
            throw new IllegalArgumentException("Note with ID" + id + " does not exist");
        }
        collectionRepository.deleteById(id);
    }

    public Collection addNoteToCollection(Long collectionId, Note noteRequest) {
        //Check whether the provided collection exists
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        collection.addNote(noteRequest);

        return collectionRepository.save(collection);
    }
}
