package objectmanager.command;

import objectmanager.service.DatabaseManager;
import objectmanager.command.result.CommandResult;

import java.util.List;

/**
 * Интерфейс команды (паттерн Command)
 */
public interface Command {

    /**
     * Выполняет команду и возвращает результат вместо прямого вывода в консоль
     *
     * @param dbManager менеджер базы данных
     * @param args аргументы команды
     * @return результат выполнения команды
     * @throws Exception при ошибке выполнения
     */
    CommandResult execute(DatabaseManager dbManager, List<String> args) throws Exception;

    /**
     * Проверяет валидность аргументов
     *
     * @param args аргументы для проверки
     * @return true если аргументы валидны, иначе false
     */
    boolean validateArgs(List<String> args);

    String getName();

    String getDescription();

    String getSyntax();

}
