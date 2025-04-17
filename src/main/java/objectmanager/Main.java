package objectmanager;

import objectmanager.cli.CommandLineInterface;
import objectmanager.service.DatabaseManager;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Основной класс приложения Использует паттерн Facade для работы с командной
 * строкой
 */
public class Main {

    public static void main(String[] args) {
        Path workingDirectory = validateAndGetWorkingDirectory(args);
        if (workingDirectory == null) {
            return;
        }

        DatabaseManager databaseManager = initializeDatabaseManager(workingDirectory);

        try (Scanner scanner = new Scanner(System.in)) {
            CommandLineInterface cli = new CommandLineInterface(
                    databaseManager,
                    scanner,
                    System.out,
                    System.err
            );

            cli.start();
        }

        System.out.println("Завершение работы Object Manager.");
    }

    private static Path validateAndGetWorkingDirectory(String[] args) {
        if (args.length != 1) {
            System.err.println("Использование: object-manager <рабочая_директория>");
            System.exit(1);
            return null;
        }

        try {
            Path workingDirectory = Paths.get(args[0]).toAbsolutePath().normalize();
            if (!workingDirectory.toFile().isDirectory()) {
                System.err.println("Ошибка: Указанный путь не является директорией: " + workingDirectory);
                System.exit(1);
                return null;
            }

            System.out.println("Используется рабочая директория: " + workingDirectory);
            return workingDirectory;
        } catch (InvalidPathException e) {
            System.err.println("Ошибка: Указан неверный путь: " + args[0]);
            System.exit(1);
            return null;
        }
    }

    /**
     * Инициализирует менеджер базы данных
     *
     * @param workingDirectory рабочая директория
     * @return инициализированный менеджер базы данных
     */
    private static DatabaseManager initializeDatabaseManager(Path workingDirectory) {
        DatabaseManager databaseManager = DatabaseManager.getInstance(workingDirectory);
        databaseManager.loadDataFromDirectory();
        return databaseManager;
    }
}
