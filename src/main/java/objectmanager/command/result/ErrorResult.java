package objectmanager.command.result;

/**
 * Результат с ошибкой (паттерн Command)
 */
public class ErrorResult implements CommandResult {

    private final String errorMessage;
    private final Exception exception;

    public ErrorResult(String errorMessage) {
        this(errorMessage, null);
    }

    public ErrorResult(String errorMessage, Exception exception) {
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }

    @Override
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ошибка: ").append(errorMessage);

        if (exception != null) {
            sb.append("\nДетали: ").append(exception.getMessage());
        }

        return sb.toString();
    }

    /**
     * @return исключение, вызвавшее ошибку (если есть)
     */
    public Exception getException() {
        return exception;
    }
}
