package objectmanager.command.result;

/**
 * Успешный результат команды (паттерн Command)
 */
public class SuccessResult implements CommandResult {

    private final String message;
    private final Object data;

    public SuccessResult(String message) {
        this(message, null);
    }

    public SuccessResult(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String format() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
