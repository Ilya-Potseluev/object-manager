package objectmanager.cli;

import objectmanager.command.Command;
import objectmanager.command.CommandFactory;
import objectmanager.command.CommandParser;
import objectmanager.command.result.CommandResult;
import objectmanager.service.DatabaseManager;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

/**
 * Интерфейс командной строки (паттерн Facade)
 */
public class CommandLineInterface {

    private final CommandParser commandParser;
    private final DatabaseManager databaseManager;
    private final Scanner scanner;
    private final PrintStream out;
    private final PrintStream err;

    public CommandLineInterface(DatabaseManager databaseManager, Scanner scanner, PrintStream out, PrintStream err) {
        this.databaseManager = databaseManager;
        this.scanner = scanner;
        this.out = out;
        this.err = err;
        this.commandParser = new CommandParser();

        CommandFactory.getInstance().initializeCommands();
    }

    public void start() {
        out.println("\nObject Manager CLI. Введите 'help' для списка команд или 'exit' для выхода.");

        while (true) {
            out.print("> ");
            String inputLine = scanner.nextLine().trim();

            if (inputLine.isEmpty()) {
                continue;
            }

            processCommand(inputLine);
        }
    }

    private void processCommand(String inputLine) {
        Optional<CommandParser.ParsedCommand> parsedCommandOpt = commandParser.parse(inputLine);

        if (parsedCommandOpt.isPresent()) {
            CommandParser.ParsedCommand parsedCommand = parsedCommandOpt.get();
            try {
                Command command = parsedCommand.getCommand();
                CommandResult result = command.execute(databaseManager, parsedCommand.getArgs());

                if (result.isSuccess()) {
                    out.println(result.format());
                } else {
                    err.println(result.format());
                }
            } catch (Exception e) {
                err.println("Ошибка при выполнении команды: " + e.getMessage());
            }
        } else {
            err.println("Неизвестная команда или неверный синтаксис. Введите 'help' для просмотра доступных команд.");
        }
    }

}
