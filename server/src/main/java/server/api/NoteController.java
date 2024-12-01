/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package server.api;

import java.util.List;
import java.util.Optional;

import commons.Note;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import server.database.NoteRepository;
import server.service.NoteService;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository repo;
    private final NoteService noteService;

    public NoteController(NoteRepository repo, NoteService noteService) {
        this.repo = repo;
        this.noteService = noteService;
    }

    @GetMapping(path = { "", "/" })
    public List<Note> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getById(@PathVariable("id") long id) {
        if (id < 0 || !repo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(repo.findById(id).get());
    }

    @PostMapping(path = { "", "/" })
    public ResponseEntity<Note> add(@RequestBody Note note) {
        Note saved = repo.save(note);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNoteById(id);
        return ResponseEntity.noContent().build();
    }

    //method that calls the updateNote in NoteService to update the selected note
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note updatedNote) {
        try{
            Note savedNote = noteService.updateNote(id, updatedNote);
            return ResponseEntity.ok(savedNote);
        }catch(RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }

}