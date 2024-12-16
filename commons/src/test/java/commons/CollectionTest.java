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
    @Test
    public void testRemoveNote() {
        testCollection3.addNote(note3);
        assertEquals(3, testCollection3.getNoteCount());
        testCollection3.removeNote(note3);
        assertEquals(2, testCollection3.getNoteCount());
        assertFalse(testCollection3.getNotes().contains(note3));
    }

    /**
     * test getNotes for out of bounds index
     */
    @Test
    public void testGetNoteOutOfBounds() {
        try {
            testCollection3.getNote(10);
            fail("Expected an IndexOutOfBoundsException to be thrown");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index 10 out of bounds for length " + testCollection3.getNoteCount(), e.getMessage());
        }
    }

    @Test
    public void testCascadingDelete() {
        testCollection3.addNote(note1);
        testCollection3.addNote(note2);

        testCollection3 = null; // Simulating deletion of the collection

        assertNull(note1.getCollection());
        assertNull(note2.getCollection());
    }

    @Test
    public void testAddDuplicateNotes() {
        testCollection3.addNote(note1);
        assertEquals(3, testCollection3.getNoteCount());
    }

    @Test
    public void testHashCodeConsistency() {
        int initialHash = testCollection3.hashCode();
        assertEquals(initialHash, testCollection3.hashCode());
        testCollection3.addNote(note3);
        assertNotEquals(initialHash, testCollection3.hashCode());
    }



}