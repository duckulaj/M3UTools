package com.hawkins.m3utoolsjpa.parser;

public class ParsingException extends RuntimeException {

    private static final long serialVersionUID = 2363427718634629974L;
	private int line;

    public ParsingException(int line, String message) {
        super(message + " at line " + line);
        this.line = line;
    }

    public ParsingException(int line, String message, Exception cause) {
        super(message + " at line " + line, cause);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
