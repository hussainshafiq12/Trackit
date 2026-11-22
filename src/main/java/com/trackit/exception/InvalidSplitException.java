package com.trackit.exception;

/**
 * Thrown when group expense split calculations violate mathematical rules.
 */
public class InvalidSplitException extends TrackitException {
    private static final long serialVersionUID = 1L;

    public InvalidSplitException(String message) {
        super(message);
    }

    public InvalidSplitException(String message, Throwable cause) {
        super(message, cause);
    }
}
