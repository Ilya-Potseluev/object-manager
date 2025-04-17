package objectmanager.command;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.service.DatabaseManager;

import java.util.List;

public class ExitCommand extends AbstractCommand {

    public ExitCommand() {
        super("exit", "Закрывает соединение и выходит из программы", "exit");
    }

    @Override
    protected CommandResult executeCommand(DatabaseManager dbManager, List<String> args) {
        System.exit(0);
        return new SuccessResult("Завершение работы...");
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.isEmpty();
    }
}
