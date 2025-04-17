package objectmanager.command;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.command.result.TableResult;
import objectmanager.service.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Команда для отображения справки по всем или указанной команде. Использует
 * паттерн Chain of Responsibility для обработки двух типов запросов
 */
public class HelpCommand extends AbstractCommand {

    private final CommandRegistry commandRegistry;

    public HelpCommand() {
        super("help", "Показывает список доступных команд", "help [команда]");
        this.commandRegistry = CommandRegistry.getInstance();
    }

    @Override
    protected CommandResult executeCommand(DatabaseManager dbManager, List<String> args) {
        if (args.isEmpty()) {
            return listAllCommands();
        } else {
            return showCommandDetails(args.get(0));
        }
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() <= 1;
    }

    private CommandResult listAllCommands() {
        TableResult.Builder builder = new TableResult.Builder()
                .withTitle("Доступные команды")
                .withHeaders(List.of("Команда", "Синтаксис", "Описание"));

        commandRegistry.getAllCommands().stream()
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .forEach(cmd -> {
                    List<String> row = new ArrayList<>();
                    row.add(cmd.getName());
                    row.add(cmd.getSyntax());
                    row.add(cmd.getDescription());
                    builder.addRow(row);
                });

        builder.withFooter("Для получения подробной информации о команде, введите: help <имя_команды>");

        return builder.build();
    }

    private CommandResult showCommandDetails(String commandName) {
        Optional<Command> commandOpt = commandRegistry.getCommand(commandName);

        if (commandOpt.isEmpty()) {
            return new SuccessResult("Команда '" + commandName + "' не найдена.\n"
                    + "Введите 'help' для просмотра доступных команд.");
        }

        Command command = commandOpt.get();
        StringBuilder sb = new StringBuilder();

        sb.append("Информация о команде: ").append(command.getName()).append("\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("Синтаксис: ").append(command.getSyntax()).append("\n");
        sb.append("Описание: ").append(command.getDescription()).append("\n");

        return new SuccessResult(sb.toString());
    }
}
