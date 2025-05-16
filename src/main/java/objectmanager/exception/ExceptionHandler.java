package objectmanager.exception;

import java.io.PrintStream;

import org.springframework.stereotype.Component;

@Component
public class ExceptionHandler {
    
    private final PrintStream errorStream;
    
    public ExceptionHandler(PrintStream errorStream) {
        this.errorStream = errorStream;
    }
    
    public void handleException(Exception e, String context) {
        String message = formatExceptionMessage(e, context);
        errorStream.println(message);
    }
    
    public RuntimeException wrapAndThrow(Exception e, String context) {
        String message = formatExceptionMessage(e, context);
        if (e instanceof RuntimeException ex) {
            return ex;
        }
        return new ApplicationException(message, e);
    }
    
    private String formatExceptionMessage(Exception e, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ошибка: ").append(context);
        
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            sb.append(" - ").append(e.getMessage());
        }
        
        // Если есть вложенное исключение, добавляем его сообщение
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            sb.append(" (").append(e.getCause().getMessage()).append(")");
        }
        
        return sb.toString();
    }
} 