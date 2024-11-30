package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionTest {

    private Collection testCollection1;
    private Collection testCollection2;
    private Collection testCollection3;
    private Note note1;
    private Note note2;
    private Note note3;

    @BeforeEach
    public void setUp() {
        testCollection1 = new Collection();
        testCollection2 = new Collection("testCollection");
        testCollection3 = new Collection("testCollection");
        note1 = new Note("this is a testNote");
        note2 = new Note("this is a testNote");
        note3 = new Note("this is testNote 3");
        testCollection2.addNote(note1);
        testCollection2.addNote(note2);
        testCollection3.addNote(note1);
        testCollection3.addNote(note2);
    }

    @Test
    public void testEquals() {
        assertEquals(testCollection3, testCollection3);
        assertNotEquals(testCollection1, testCollection3);
    }

    @Test
    public void testHashCode() {
        assertEquals(testCollection2.hashCode(), testCollection2.hashCode());
        assertNotEquals(testCollection2.hashCode(), testCollection1.hashCode());
    }

    @Test
    public void testAddNoteAndGetNote() {
        assertEquals(testCollection3, testCollection3);
        testCollection3.addNote(note3);
        assertEquals(note3, testCollection3.getNote(2));
}
}