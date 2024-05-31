package bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception;

public class DataStorageException extends Exception {

    public DataStorageException(String message) {
        super(message);
    }

    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}