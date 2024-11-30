package commons;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    private String collection;
    public String content;

    public Note() {

    }

    public Note(String content) {
        this.content = content;
    }

    public Note(String collection, String content) {
        this.collection = collection;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id == note.id && Objects.equals(content, note.content)
                && Objects.equals(collection, note.collection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, collection);
}

}