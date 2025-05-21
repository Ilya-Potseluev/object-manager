package objectmanager.config;

import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import objectmanager.exception.ExceptionHandler;
import objectmanager.repository.TableRepository;
import objectmanager.service.AsyncService;

@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig {

    @Value("${app.working-directory}")
    private String workingDirectoryPath;

    @Value("${app.threads.max-pool-size:10}")
    private int maxPoolSize;

    @Bean
    public Path workingDirectory() {
        try {
            Path workingDirectory = Paths.get(workingDirectoryPath).toAbsolutePath().normalize();
            if (!workingDirectory.toFile().isDirectory()) {
                throw new IllegalArgumentException("Указанный путь не является директорией: " + workingDirectory);
            }
            System.out.println("Используется рабочая директория: " + workingDirectory);
            return workingDirectory;
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Указан неверный путь: " + workingDirectoryPath, e);
        }
    }

    @Bean
    public ExecutorService taskExecutor() {
        return Executors.newFixedThreadPool(
                maxPoolSize,
                new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "object-manager-task-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        }
        );
    }

    @Bean
    public ScheduledExecutorService scheduledExecutor() {
        return Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "object-manager-scheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Bean
    public AsyncService asyncService(ExecutorService taskExecutor, ScheduledExecutorService scheduledExecutor) {
        return new AsyncService(taskExecutor, scheduledExecutor);
    }

    @Bean
    public TableRepository tableRepository(Path workingDirectory, AsyncService asyncService, ExceptionHandler exceptionHandler) {
        TableRepository tableRepository = new TableRepository(workingDirectory, asyncService, exceptionHandler);
        tableRepository.initialize();
        tableRepository.loadAllTables();
        return tableRepository;
    }

    @Bean
    public Scanner scanner() {
        return new Scanner(System.in);
    }

    @Bean
    public PrintStream outputStream() {
        return System.out;
    }

    @Bean
    public PrintStream errorStream() {
        return System.err;
    }

    @Bean
    public ExceptionHandler exceptionHandler(PrintStream errorStream) {
        return new ExceptionHandler(errorStream);
    }
}
