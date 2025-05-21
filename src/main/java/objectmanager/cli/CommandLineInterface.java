package objectmanager.cli;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import objectmanager.command.Command;
import objectmanager.command.CommandParser;
import objectmanager.command.CommandRegistry;
import objectmanager.command.result.CommandResult;
import objectmanager.exception.ExceptionHandler;
import objectmanager.repository.TableRepository;
import objectmanager.service.AsyncService;

/**
 * Интерфейс командной строки (паттерн Facade)
 */
@Component
public class CommandLineInterface {

    private final CommandParser commandParser;
    private final TableRepository tableRepository;
    private final Scanner scanner;
    private final PrintStream out;
    private final PrintStream err;
    private final AsyncService asyncService;
    private final ExceptionHandler exceptionHandler;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public CommandLineInterface(TableRepository tableRepository, Scanner scanner,
            @Qualifier("outputStream") PrintStream out, @Qualifier("errorStream") PrintStream err,
            AsyncService asyncService, ExceptionHandler exceptionHandler, CommandRegistry commandRegistry, CommandParser commandParser) {
        this.tableRepository = tableRepository;
        this.scanner = scanner;
        this.out = out;
        this.err = err;
        this.asyncService = asyncService;
        this.exceptionHandler = exceptionHandler;
        this.commandParser = commandParser;

        commandRegistry.initializeAllCommands();
    }

    public void start() {
        out.println("\nObject Manager CLI. Введите 'help' для списка команд или 'exit' для выхода.");

        while (running.get()) {
            out.print("> ");
            String inputLine = scanner.nextLine().trim();

            if (inputLine.isEmpty()) {
                continue;
            }

            processCommand(inputLine);
        }

        try {
            out.println("Сохранение данных перед выходом...");
            tableRepository.saveAllTables();
            asyncService.shutdown();
        } catch (Exception e) {
            exceptionHandler.handleException(e, "Ошибка при сохранении данных");
        }
    }

    private void processCommand(String inputLine) {
        Optional<CommandParser.ParsedCommand> parsed = commandParser.parse(inputLine);

        if (parsed.isPresent()) {
            CommandParser.ParsedCommand parsedCommand = parsed.get();
            try {
                Command command = parsedCommand.getCommand();

                if ("exit".equals(command.getName())) {
                    running.set(false);
                    return;
                }

                CommandResult result = command.execute(tableRepository, parsedCommand.getArgs());

                if (result.isSuccess()) {
                    out.println(result.format());
                } else {
                    err.println(result.format());
                }
            } catch (Exception e) {
                exceptionHandler.handleException(e, "Ошибка при выполнении команды");
            }
        } else {
            err.println("Неизвестная команда или неверный синтаксис. Введите 'help' для просмотра доступных команд.");
        }
    }
}
