package com.trackit.exception;

/**
 * Base checked exception for Trackit application.
 */
public class TrackitException extends Exception {
    private static final long serialVersionUID = 1L;

    public TrackitException(String message) {
        super(message);
    }

    public TrackitException(String message, Throwable cause) {
        super(message, cause);
    }
}
