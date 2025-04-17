package objectmanager.command;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр команд (паттерн Registry) Реализует также паттерн Singleton для
 * глобального доступа
 */
public class CommandRegistry {

    private static volatile CommandRegistry instance;
    private final Map<String, Command> commandMap = new ConcurrentHashMap<>();

    static {
        instance = new CommandRegistry();
    }

    public static CommandRegistry getInstance() {
        return instance;
    }

    /**
     * Регистрация новой команды
     *
     * @param command команда для регистрации
     * @return true если команда была зарегистрирована, false если имя уже
     * занято
     */
    public boolean registerCommand(Command command) {
        Objects.requireNonNull(command, "Command cannot be null");
        String commandName = command.getName().toLowerCase();

        if (commandMap.containsKey(commandName)) {
            return false;
        }

        commandMap.put(commandName, command);
        return true;
    }

    public Command unregisterCommand(String commandName) {
        Objects.requireNonNull(commandName, "Command name cannot be null");
        return commandMap.remove(commandName.toLowerCase());
    }

    public Optional<Command> getCommand(String commandName) {
        Objects.requireNonNull(commandName, "Command name cannot be null");
        return Optional.ofNullable(commandMap.get(commandName.toLowerCase()));
    }

    public Collection<Command> getAllCommands() {
        return Collections.unmodifiableCollection(commandMap.values());
    }

    public void clearCommands() {
        commandMap.clear();
    }
}
