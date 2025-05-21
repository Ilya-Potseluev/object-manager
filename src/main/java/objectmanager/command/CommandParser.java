package objectmanager.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Парсер команд, преобразующий строковый ввод в команды
 */
@Component
public class CommandParser {

    private final CommandRegistry commandRegistry;

    @Autowired
    public CommandParser(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    public static class ParsedCommand {

        private final Command command;
        private final List<String> args;

        public ParsedCommand(Command command, List<String> args) {
            this.command = command;
            this.args = Collections.unmodifiableList(new ArrayList<>(args));
        }

        public Command getCommand() {
            return command;
        }

        public List<String> getArgs() {
            return args;
        }
    }

    /**
     * Парсит строку ввода и возвращает команду с аргументами
     *
     * @param inputLine строка ввода
     * @return Optional с ParsedCommand или пустой Optional если команда не
     * найдена
     */
    public Optional<ParsedCommand> parse(String inputLine) {
        if (inputLine == null || inputLine.trim().isEmpty()) {
            return Optional.empty();
        }

        String[] parts = inputLine.split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        List<String> args = (parts.length > 1)
                ? Arrays.asList(parts[1].split("\\s+")) : List.of();

        Optional<Command> commandOpt = commandRegistry.getCommand(commandName);

        return commandOpt.map(command -> new ParsedCommand(command, args));
    }
}
