package com.trackit.exception;

/**
 * Thrown when persistent data store operations encounter I/O or serialization failures.
 */
public class DataPersistenceException extends TrackitException {
    private static final long serialVersionUID = 1L;

    public DataPersistenceException(String message) {
        super(message);
    }

    public DataPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
