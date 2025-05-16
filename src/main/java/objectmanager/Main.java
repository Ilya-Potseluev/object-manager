package objectmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import objectmanager.cli.CommandLineInterface;
import objectmanager.command.ExitCommand;

/**
 * Основной класс приложения Использует паттерн Facade для работы с командной
 * строкой
 */
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Использование: object-manager <рабочая_директория>");
            System.exit(1);
        }

        System.setProperty("app.working-directory", args[0]);
        try (ConfigurableApplicationContext context = SpringApplication.run(Main.class, args)) {
            ExitCommand.setApplicationContext(context);
            
            CommandLineInterface cli = context.getBean(CommandLineInterface.class);
            cli.start();
            
            System.out.println("Завершение работы Object Manager.");
        }
    }
}
