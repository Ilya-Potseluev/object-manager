package objectmanager.command;

import java.util.List;

import objectmanager.command.result.CommandResult;
import objectmanager.repository.TableRepository;

/**
 * Интерфейс команды (паттерн Command)
 */
public interface Command {

    /**
     * Выполняет команду и возвращает результат вместо прямого вывода в консоль
     *
     * @param tableRepository репозиторий таблиц
     * @param args аргументы команды
     * @return результат выполнения команды
     * @throws Exception при ошибке выполнения
     */
    CommandResult execute(TableRepository tableRepository, List<String> args) throws Exception;

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
