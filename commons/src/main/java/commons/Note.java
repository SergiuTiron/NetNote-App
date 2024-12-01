package commons;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "my_sequence2", allocationSize = 1)
    public long id;

    public static int numberOfNotes = 0;
    public int noteNumber;
    private String title;
    public String content;

    public Note() {

    }

    public Note(String content) {
        this.content = content;
        numberOfNotes++;
        this.noteNumber = numberOfNotes;
        this.title = "Note " + this.noteNumber;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public static void decrementNumberOfNotes(){
        numberOfNotes--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id == note.id && noteNumber == note.noteNumber && Objects.equals(title, note.title) && Objects.equals(content, note.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, noteNumber, title, content);
    }
}