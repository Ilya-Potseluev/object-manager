package objectmanager.command;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.repository.TableRepository;

/**
 * Команда для отображения справки по всем или указанной команде. Использует
 * паттерн Chain of Responsibility для обработки двух типов запросов
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HelpCommand extends AbstractCommand {

    private final CommandRegistry commandRegistry;

    @Autowired
    public HelpCommand(CommandRegistry commandRegistry) {
        super("help", "Выводит справку по доступным командам", "help [команда]");
        this.commandRegistry = commandRegistry;
    }

    @Override
    protected CommandResult executeCommand(TableRepository tableRepository, List<String> args) {
        StringBuilder helpText = new StringBuilder();

        if (args.isEmpty()) {
            helpText.append("Доступные команды:\n");

            Collection<Command> commands = commandRegistry.getAllCommands();

            for (Command command : commands) {
                helpText.append("  ").append(command.getName())
                        .append(" - ").append(command.getDescription())
                        .append("\n");
            }

            helpText.append("\nИспользуйте 'help <команда>' для получения подробной информации по конкретной команде.");
        } else {
            String commandName = args.get(0);
            Optional<Command> commandOpt = commandRegistry.getCommand(commandName);

            if (commandOpt.isEmpty()) {
                return new SuccessResult("Команда '" + commandName + "' не найдена.");
            }

            Command command = commandOpt.get();
            helpText.append("Команда: ").append(command.getName()).append("\n");
            helpText.append("Описание: ").append(command.getDescription()).append("\n");
            helpText.append("Синтаксис: ").append(command.getSyntax()).append("\n");
        }

        return new SuccessResult(helpText.toString());
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return true;
    }
}
