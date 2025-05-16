package objectmanager.exception;

public class ApplicationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public ApplicationException(String message) {
        super(message);
    }
    
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ApplicationException(Throwable cause) {
        super(cause);
    }
} 