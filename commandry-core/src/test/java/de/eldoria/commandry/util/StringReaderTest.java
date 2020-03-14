package de.eldoria.commandry.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringReaderTest {

    @Test
    void testEmptyString() {
        StringReader reader = reader("");
        assertFalse(reader.canRead());
        assertThrows(StringIndexOutOfBoundsException.class, reader::readWord);
    }

    @Test
    void testBlankString() {
        StringReader reader = reader(" ");
        assertTrue(reader.canRead());
        assertEquals("", reader.readWord());
    }

    @Test
    void testBigBlankString() {
        assertEquals("", reader(" ".repeat(65535)).readWord());
    }

    @Test
    void testSingleWord() {
        assertEquals("hello", reader("hello").readWord());
    }

    @Test
    void testSingleWordWithLeadingSpace() {
        assertEquals("hello", reader(" hello").readWord());
    }

    @Test
    void testSingleWordWithTrailingSpace() {
        assertEquals("hello", reader("hello ").readWord());
    }

    @Test
    void testMultiWordSingleSpaceSeparated() {
        StringReader reader = reader("hello world");
        assertEquals("hello", reader.readWord());
        assertEquals("world", reader.readWord());
    }

    @Test
    void testMultiWordMultiSpaceSeparated() {
        StringReader reader = reader("hello  world");
        assertEquals("hello", reader.readWord());
        assertEquals("world", reader.readWord());
    }

    @Test
    void testSentence() {
        String[] sentence = new String[] {
                "Lorem", "ipsum", "dolor", "sit", "amet,", "consetetur", "sadipscing", "elitr,",
                "sed", "diam", "nonumy", "eirmod", "tempor", "invidunt", "ut", "labore"
        };
        StringReader reader = reader(String.join(" ", sentence));
        for (String expected : sentence) {
            assertEquals(expected, reader.readWord());
        }
    }

    @Test
    void testReadRemainingEmpty() {
        assertEquals("", reader("").readRemaining());
    }

    @Test
    void testRemainingSame() {
        String all = " hello  world ";
        assertSame(all, reader(all).readRemaining());
    }

    @Test
    void testRemainingSameAfterReset() {
        String all = " hello  world ";
        StringReader reader = reader(all);
        assertEquals("hello", reader.readWord());
        reader.reset();
        assertSame(all, reader.readRemaining());
    }

    @Test
    void testReset() {
        StringReader reader = reader("hello");
        assertEquals("hello", reader.readWord());
        assertFalse(reader.canRead());
        assertThrows(StringIndexOutOfBoundsException.class, reader::readWord);

        reader.reset();
        assertTrue(reader.canRead());

        assertEquals("hello", reader.readWord());
        assertThrows(StringIndexOutOfBoundsException.class, reader::readWord);
        assertFalse(reader.canRead());
    }

    private StringReader reader(String input) {
        return new StringReader(input);
    }

}