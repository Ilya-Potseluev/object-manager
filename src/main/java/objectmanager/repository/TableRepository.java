package objectmanager.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;

import objectmanager.exception.ApplicationException;
import objectmanager.exception.ExceptionHandler;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.persistence.DataSaver;
import objectmanager.persistence.SchemaLoader;
import objectmanager.persistence.TableLoader;
import objectmanager.service.AsyncService;

@Repository
public class TableRepository {

    private final Path workingDirectory;
    private final Path tablesDirectory;
    private final Path dataDirectory;
    private final SchemaLoader schemaLoader;
    private final TableLoader tableLoader;
    private final DataSaver dataSaver;
    private final Map<String, DataTable> tables = new ConcurrentHashMap<>();
    private final Map<String, Boolean> dirtyTables = new ConcurrentHashMap<>();
    private final AsyncService asyncService;
    private final ExceptionHandler exceptionHandler;
    
    private ScheduledFuture<?> autosaveTask;

    public TableRepository(Path workingDirectory, AsyncService asyncService, ExceptionHandler exceptionHandler) {
        this.workingDirectory = workingDirectory;
        this.tablesDirectory = workingDirectory.resolve("tables");
        this.dataDirectory = workingDirectory.resolve("data");
        this.schemaLoader = new SchemaLoader();
        this.tableLoader = new TableLoader();
        this.dataSaver = new DataSaver();
        this.asyncService = asyncService;
        this.exceptionHandler = exceptionHandler;
    }

    public void initialize() {
        try {
            Files.createDirectories(tablesDirectory);
            Files.createDirectories(dataDirectory);
            startAutosaveTask();
        } catch (IOException e) {
            throw new ApplicationException("Не удалось создать необходимые директории: " + 
                tablesDirectory + " или " + dataDirectory, e);
        }
    }
    
    private void startAutosaveTask() {
        autosaveTask = asyncService.scheduleAtFixedRate(() -> {
            try {
                saveModifiedTables();
            } catch (Exception e) {
                exceptionHandler.handleException(e, "Ошибка при автосохранении таблиц");
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    private synchronized void saveModifiedTables() throws IOException {
        if (dirtyTables.isEmpty()) {
            return;
        }
        
        for (Map.Entry<String, Boolean> entry : dirtyTables.entrySet()) {
            if (entry.getValue()) {
                String tableName = entry.getKey();
                DataTable table = tables.get(tableName);
                if (table != null) {
                    try {
                        dataSaver.saveData(dataDirectory, table);
                    } catch (IOException e) {
                        exceptionHandler.handleException(e, "Не удалось сохранить данные таблицы: " + tableName);
                        throw e;
                    }
                }
            }
        }
        dirtyTables.clear();
    }

    public void loadAllTables() {
        try (Stream<Path> paths = Files.list(tablesDirectory)) {
            List<Path> schemaFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            
            for (Path schemaFile : schemaFiles) {
                try {
                    Optional<TableSchema> schema = schemaLoader.loadSchema(schemaFile);
                    if (schema.isEmpty()) {
                        exceptionHandler.handleException(
                            new ApplicationException("Неверный формат схемы"),
                            "Не удалось загрузить схему из файла: " + schemaFile
                        );
                        continue;
                    }
                    
                    TableSchema tableSchema = schema.get();
                    String tableName = tableSchema.getTableName();
                    
                    Optional<DataTable> data = tableLoader.loadData(dataDirectory, tableSchema);
                    if (data.isEmpty()) {
                        exceptionHandler.handleException(
                            new ApplicationException("Ошибка загрузки данных"),
                            "Не удалось загрузить данные для таблицы: " + tableName
                        );
                        continue;
                    }
                    
                    tables.put(tableName, data.get());
                } catch (Exception e) {
                    exceptionHandler.handleException(e, "Ошибка при обработке файла: " + schemaFile);
                }
            }
        } catch (IOException e) {
            exceptionHandler.handleException(e, "Ошибка чтения файлов схем в " + tablesDirectory);
        }
    }

    public Optional<DataTable> findTable(String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }

    public Map<String, DataTable> getAllTables() {
        return Collections.unmodifiableMap(tables);
    }

    public CompletableFuture<Boolean> saveTableAsync(String tableName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return saveTable(tableName);
            } catch (IOException e) {
                exceptionHandler.handleException(e, "Ошибка при сохранении таблицы " + tableName);
                throw new ApplicationException("Ошибка сохранения", e);
            }
        }, asyncService.getExecutor())
        .exceptionally(ex -> {
            exceptionHandler.handleException((Exception)ex, "Неожиданная ошибка при сохранении таблицы " + tableName);
            return false;
        });
    }

    private void saveData(Path filePath, DataTable table) throws IOException {
        if (Files.exists(filePath) && table.getObjectCount() == 0) {
            exceptionHandler.handleException(
                new ApplicationException("Предотвращена попытка сохранения пустых данных"),
                "Отменено сохранение пустых данных в существующий файл: " + filePath
            );
            return;
        }
        dataSaver.saveData(dataDirectory, table);
    }

    public boolean saveTable(String tableName) throws IOException {
        DataTable table = tables.get(tableName);
        if (table == null) {
            return false;
        }
        
        Path dataPath = dataDirectory.resolve(tableName + ".json");
        try {
            saveData(dataPath, table);
            dirtyTables.remove(tableName);
            return true;
        } catch (IOException e) {
            exceptionHandler.handleException(e, "Ошибка при сохранении таблицы " + tableName);
            throw e;
        }
    }

    public CompletableFuture<Void> saveAllTablesAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                saveAllTables();
            } catch (IOException e) {
                exceptionHandler.handleException(e, "Ошибка при сохранении таблиц");
                throw new ApplicationException("Ошибка сохранения таблиц", e);
            }
        }, asyncService.getExecutor())
        .exceptionally(ex -> {
            exceptionHandler.handleException((Exception)ex, "Неожиданная ошибка при сохранении таблиц");
            return null;
        });
    }

    public void saveAllTables() throws IOException {
        for (Map.Entry<String, DataTable> entry : tables.entrySet()) {
            try {
                String tableName = entry.getKey();
                DataTable table = entry.getValue();
                Path dataPath = dataDirectory.resolve(tableName + ".json");
                
                saveData(dataPath, table);
            } catch (IOException e) {
                exceptionHandler.handleException(e, "Ошибка при сохранении таблицы " + entry.getKey());
                throw e;
            }
        }
        dirtyTables.clear();
    }

    public boolean createTable(TableSchema schema) throws IOException {
        String tableName = schema.getTableName();
        if (tables.containsKey(tableName)) {
            return false;
        }
        
        DataTable newTable = new DataTable(schema);
        tables.put(tableName, newTable);
        
        CompletableFuture<Void> schemaFuture = CompletableFuture.runAsync(() -> {
            try {
                dataSaver.saveSchema(tablesDirectory, schema);
            } catch (IOException e) {
                exceptionHandler.handleException(e, "Ошибка при сохранении схемы таблицы " + tableName);
                throw new ApplicationException("Ошибка сохранения схемы", e);
            }
        }, asyncService.getExecutor());
        
        CompletableFuture<Void> dataFuture = CompletableFuture.runAsync(() -> {
            try {
                dataSaver.saveData(dataDirectory, newTable);
            } catch (IOException e) {
                exceptionHandler.handleException(e, "Ошибка при сохранении данных таблицы " + tableName);
                throw new ApplicationException("Ошибка сохранения данных", e);
            }
        }, asyncService.getExecutor());
        
        try {
            CompletableFuture.allOf(schemaFuture, dataFuture).join();
            return true;
        } catch (Exception e) {
            tables.remove(tableName);
            throw new IOException("Ошибка при создании таблицы: " + e.getMessage(), e);
        }
    }

    public boolean dropTable(String tableName) throws IOException {
        DataTable table = tables.remove(tableName);
        if (table == null) {
            return false;
        }
        
        dirtyTables.remove(tableName);
        
        Path schemaPath = tablesDirectory.resolve(tableName + ".json");
        Path dataPath = dataDirectory.resolve(tableName + ".json");
        
        CompletableFuture.runAsync(() -> {
            try {
                Files.deleteIfExists(schemaPath);
                Files.deleteIfExists(dataPath);
            } catch (IOException e) {
                exceptionHandler.handleException(e, "Ошибка при удалении файлов таблицы " + tableName);
            }
        }, asyncService.getExecutor());
        
        return true;
    }
    
    public void markTableAsDirty(String tableName) {
        dirtyTables.put(tableName, true);
    }
}
