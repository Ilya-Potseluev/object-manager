package objectmanager.command;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.repository.TableRepository;

/**
 * Команда для завершения работы приложения
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExitCommand extends AbstractCommand {

    private static ApplicationContext applicationContext;

    @Autowired
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public ExitCommand() {
        super("exit", "Сохраняет данные и завершает работу приложения", "exit");
    }

    @Override
    protected CommandResult executeCommand(TableRepository tableRepository, List<String> args) throws Exception {
        try {
            tableRepository.saveAllTables();

            if (applicationContext instanceof ConfigurableApplicationContext) {
                Thread shutdownThread = new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        ((ConfigurableApplicationContext) applicationContext).close();
                        System.exit(0);
                    } catch (InterruptedException e) {
                    }
                });
                shutdownThread.setDaemon(true);
                shutdownThread.start();
            }

            return new SuccessResult("Данные сохранены. Завершение работы...");
        } catch (Exception e) {
            return new SuccessResult("Ошибка при сохранении данных: " + e.getMessage() + ". Завершение работы...");
        }
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.isEmpty();
    }
}
