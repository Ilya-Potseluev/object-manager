package objectmanager.command;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.ErrorResult;
import objectmanager.service.DatabaseManager;

import java.util.List;

/**
 * Базовая реализация команды с шаблонным методом (паттерн Template Method)
 */
public abstract class AbstractCommand implements Command {

    private final String name;
    private final String description;
    private final String syntax;

    protected AbstractCommand(String name, String description, String syntax) {
        this.name = name;
        this.description = description;
        this.syntax = syntax;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getSyntax() {
        return syntax;
    }

    /**
     * Шаблонный метод (паттерн Template Method), определяющий алгоритм
     * выполнения команды с валидацией аргументов
     */
    @Override
    public final CommandResult execute(DatabaseManager dbManager, List<String> args) throws Exception {

        if (!validateArgs(args)) {
            return new ErrorResult("Неверные аргументы. Использование: " + getSyntax());
        }

        try {

            return executeCommand(dbManager, args);
        } catch (Exception e) {
            return new ErrorResult("Ошибка при выполнении команды: " + e.getMessage(), e);
        }
    }

    protected abstract CommandResult executeCommand(DatabaseManager dbManager, List<String> args) throws Exception;
}
