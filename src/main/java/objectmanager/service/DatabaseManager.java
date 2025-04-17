package objectmanager.service;

import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.repository.TableRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Менеджер базы данных, управляющий таблицами в памяти. Реализует паттерн
 * Singleton
 */
public class DatabaseManager {

    private static volatile DatabaseManager instance;
    private final Path workingDirectory;
    private final TableRepository tableRepository;

    private DatabaseManager(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.tableRepository = new TableRepository(workingDirectory);
        this.tableRepository.initialize();
    }

    public static DatabaseManager getInstance(Path workingDirectory) {
        DatabaseManager localInstance = instance;
        if (localInstance == null) {
            synchronized (DatabaseManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DatabaseManager(workingDirectory);
                }
            }
        }
        return localInstance;
    }

    public void loadDataFromDirectory() {
        tableRepository.loadAllTables();
    }

    public Optional<DataTable> getTable(String tableName) {
        return tableRepository.findTable(tableName);
    }

    public Map<String, DataTable> getAllTables() {
        return tableRepository.getAllTables();
    }

    public boolean saveTable(String tableName) throws IOException {
        return tableRepository.saveTable(tableName);
    }

    public void saveAllTables() throws IOException {
        tableRepository.saveAllTables();
    }

    public boolean createTable(TableSchema schema) throws IOException {
        return tableRepository.createTable(schema);
    }

    public boolean dropTable(String tableName) throws IOException {
        return tableRepository.dropTable(tableName);
    }

    public Path getWorkingDirectory() {
        return workingDirectory;
    }
}
