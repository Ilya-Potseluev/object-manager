package objectmanager.command;

import java.util.List;

import objectmanager.command.result.CommandResult;
import objectmanager.repository.TableRepository;

/**
 * Интерфейс команды (паттерн Command)
 */
public interface Command {

    CommandResult execute(TableRepository tableRepository, List<String> args) throws Exception;

    boolean validateArgs(List<String> args);

    String getName();

    String getDescription();

    String getSyntax();

}
