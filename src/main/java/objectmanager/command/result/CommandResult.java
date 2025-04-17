package objectmanager.command.result;

/**
 * Интерфейс результата выполнения команды (паттерн Command)
 */
public interface CommandResult {

    boolean isSuccess();

    String getMessage();

    String format();
}
