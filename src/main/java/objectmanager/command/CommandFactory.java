package objectmanager.command;

/**
 * Фабрика для создания команд (паттерн Factory Method)
 */
public class CommandFactory {

    private static final CommandFactory instance;

    static {
        instance = new CommandFactory();
    }

    public static CommandFactory getInstance() {
        return instance;
    }

    public void initializeCommands() {
        CommandRegistry registry = CommandRegistry.getInstance();

        registry.clearCommands();

        Command[] commandList = new Command[]{
            new HelpCommand(),
            new ListCommand(),
            new ShowCommand(),
            new ExitCommand(),
            new InsertCommand(),
            new SelectCommand(),
            new CreateCommand()
        };

        for (Command command : commandList) {
            registry.registerCommand(command);
        }
    }

}
