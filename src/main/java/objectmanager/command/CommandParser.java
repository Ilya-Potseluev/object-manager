package objectmanager.command;

import java.util.*;

/**
 * Парсер команд, преобразующий строковый ввод в команды Использует паттерн
 * Facade для скрытия сложности работы с командами
 */
public class CommandParser {

    private final CommandRegistry commandRegistry;

    public CommandParser() {
        this.commandRegistry = CommandRegistry.getInstance();
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
