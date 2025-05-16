package objectmanager.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

@Service
public class AsyncService {

    private final ExecutorService taskExecutor;
    private final ScheduledExecutorService scheduledExecutor;

    public AsyncService(ExecutorService taskExecutor, ScheduledExecutorService scheduledExecutor) {
        this.taskExecutor = taskExecutor;
        this.scheduledExecutor = scheduledExecutor;
    }

    public <T> CompletableFuture<T> executeAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, taskExecutor);
    }

    public CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(task, taskExecutor);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return scheduledExecutor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }
    
    public ExecutorService getExecutor() {
        return taskExecutor;
    }
    
    public ScheduledExecutorService getScheduler() {
        return scheduledExecutor;
    }
    
    public void shutdown() {
        taskExecutor.shutdown();
        scheduledExecutor.shutdown();
    }
} 