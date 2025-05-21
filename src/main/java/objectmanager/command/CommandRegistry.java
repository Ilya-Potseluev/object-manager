package objectmanager.command;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Реестр команд (паттерн Registry)
 */
@Component
public class CommandRegistry {

    private final Map<String, Class<? extends Command>> commandMap = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext context;

    /**
     * Инициализирует все команды из контекста Spring
     */
    public void initializeAllCommands() {
        if (context == null) {
            return;
        }

        commandMap.clear();

        Map<String, Command> commandBeans = context.getBeansOfType(Command.class);

        for (Command command : commandBeans.values()) {
            registerCommand(command);
        }
    }

    public boolean registerCommand(Command command) {
        Objects.requireNonNull(command, "Command cannot be null");
        String commandName = command.getName().toLowerCase();
        Class<? extends Command> commandClass = command.getClass();

        if (commandMap.containsKey(commandName)) {
            return false;
        }

        commandMap.put(commandName, commandClass);
        return true;
    }

    public Optional<Command> getCommand(String commandName) {
        Objects.requireNonNull(commandName, "Command name cannot be null");
        Class<? extends Command> commandClass = commandMap.get(commandName.toLowerCase());
        if (commandClass != null && context != null) {
            return Optional.of(context.getBean(commandClass));
        }
        return Optional.empty();
    }

    public Collection<Command> getAllCommands() {
        if (context == null) {
            return Collections.emptyList();
        }

        return commandMap.keySet().stream()
                .map(name -> getCommand(name).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
