package com.trackit.exception;

/**
 * Base domain runtime exception for Trackit application.
 * Enables defensive programming across controllers, strategies, and persistence.
 */
public class TrackitException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TrackitException(String message) {
        super(message);
    }

    public TrackitException(String message, Throwable cause) {
        super(message, cause);
    }
}
