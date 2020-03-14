package de.eldoria.commandry.util;

/**
 * The {@code StringReader} class helps to read a raw input string.
 * It provides functionality to read single words successively.
 * As it has an internal state, a new object is required for each string to read.
 */
public class StringReader {
    private final String string;
    private final int length;
    private int position;
    private int mark;

    /**
     * Creates a new StringReader to read the given string.
     *
     * @param string the string to read.
     * @throws NullPointerException if the given string is {@code null}.
     */
    public StringReader(String string) {
        this.string = string;
        this.length = string.length();
        this.position = 0;
        this.mark = 0;
    }

    /**
     * Reads the next word of the given string. A word is a sequence of characters without whitespaces
     * in between.
     * For example
     * {@code Hello World} has the words {@code Hello} and @{code World}. {@code HelloWorld} is NOT a
     * word in the given string.
     * </p>
     * Multiple whitespaces in a row will be skipped and therefore not be included in a resulting string.
     * That means, you can assume that {@code String#contains(" ")} is false for each returned word.
     * If {@link #canRead()} returns false, a {@link StringIndexOutOfBoundsException} will be thrown.
     * This can either be caused by an empty string or if the string is already read completely.
     * If the string is blank, means it only contains whitespaces, an empty string will be returned.
     *
     * @return the next word in the underlying string.
     * @throws StringIndexOutOfBoundsException if the underlying string is empty or already read completely.
     */
    public String readWord() {
        if (!canRead()) {
            throw new StringIndexOutOfBoundsException(position);
        }
        while (position < length && peekChar() == ' ') { // ignore leading spaces
            position++;
        }
        int start = position;
        while (position < length && peekChar() != ' ') {
            position++;
        }
        int end = position;
        if (position < length) {
            position++; // skip space
        }
        return string.substring(start, end);
    }

    /**
     * Reads the whole remaining string and returns all read characters as a string.
     * If nothing was read from the string or the reader was reset to 0, the underlying string will
     * be returned.
     * If {@link #canRead()} returns false, a {@link StringIndexOutOfBoundsException} will be thrown.
     * After calling this method, {@link #canRead()} will return false as long as the reader wasn't reset.
     *
     * @return the remaining string.
     * @see #reset()
     */
    public String readRemaining() {
        if (position == 0) {
            return string;
        }
        if (!canRead()) {
            throw new StringIndexOutOfBoundsException(position);
        }
        int start = position;
        position = length;
        return string.substring(start);
    }

    /**
     * Checks whether any more can be read from the string. In that case, {@link #readWord()} and
     * {@link #readRemaining()} can be called safely without throwing an exception.
     *
     * @return {@code true} if anything else can be read from the string, {@code false} otherwise.
     */
    public boolean canRead() {
        return position < length;
    }

    /**
     * Resets the reader to the beginning of the string. This can be used if the same string needs
     * to be read again, so no new instance of {@code StringReader} is required.
     */
    public void reset() {
        this.position = mark;
    }

    @Override
    public String toString() {
        return "StringReader(\"" + string + "\")";
    }

    private char peekChar() {
        return string.charAt(position);
    }
}
