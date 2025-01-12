package server.api;

import commons.Collection;
import commons.Note;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.CollectionRepository;
import server.service.CollectionService;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    private final CollectionRepository repo;
    private final CollectionService collectionService;

    public CollectionController(CollectionRepository repo, CollectionService collectionService) {
        this.repo = repo;
        this.collectionService = collectionService;
    }

    @GetMapping(path = { "", "/" })
    public List<Collection> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{collectionName}")
    public ResponseEntity<Collection> get(@PathVariable String collectionName) {
        if(repo.findByName(collectionName).isPresent()) {
            return ResponseEntity.ok(repo.findByName(collectionName).get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{collectionId}/notes")
    public ResponseEntity<List<Note>> getNotes(@PathVariable Long collectionId) {
        if (repo.findById(collectionId).isPresent()) {
            return ResponseEntity.ok(repo.findById(collectionId).get().getNotes());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping(path = { "", "/" })
    public ResponseEntity<Collection> addCollection(@RequestBody Collection collection) {
        Collection saved = repo.save(collection);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{collectionId}")
    public ResponseEntity<Collection> addNote(@PathVariable Long collectionId, @RequestBody Note noteRequest) {
        Collection updatedCollection = collectionService.addNoteToCollection(collectionId, noteRequest);
        return ResponseEntity.ok(updatedCollection);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        Long defaultCollectionId = getDefaultCollection().getBody().getId();
        if (id.equals(defaultCollectionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        collectionService.deleteCollectionByID(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/default")
    public ResponseEntity<Collection> getDefaultCollection() {
        Collection defaultCollection = collectionService.getOrCreateDefaultCollection();
        return ResponseEntity.ok(defaultCollection);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Collection> updateCollection(@PathVariable Long id, @RequestBody Collection collection) {
        try {
            Collection updatedCollection = collectionService.updateCollection(id, collection);
            return ResponseEntity.ok(updatedCollection);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


}
