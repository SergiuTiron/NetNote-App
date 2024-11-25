package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class NoteTest {

    @Test
    public void testConstructor() {
        Note note = new Note("abcdefg");
        assertEquals("abcdefg", note.content);
    }

    @Test
    public void testEqualsNotEquals() {
        Note note1 = new Note("abcdefg");
        Note note2 = new Note("abcdefg");
        assertEquals(note1, note2);

        Note note3 = new Note("xyz");
        assertNotEquals(note2, note3);
    }

}
