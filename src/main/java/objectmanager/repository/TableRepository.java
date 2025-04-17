package objectmanager.repository;

import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.persistence.DataLoader;
import objectmanager.persistence.DataSaver;
import objectmanager.persistence.SchemaLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class TableRepository {

    private final Path workingDirectory;
    private final Path tablesDirectory;
    private final Path dataDirectory;
    private final SchemaLoader schemaLoader;
    private final DataLoader dataLoader;
    private final DataSaver dataSaver;
    private final Map<String, DataTable> tables = new ConcurrentHashMap<>();

    public TableRepository(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.tablesDirectory = workingDirectory.resolve("tables");
        this.dataDirectory = workingDirectory.resolve("data");
        this.schemaLoader = new SchemaLoader();
        this.dataLoader = new DataLoader();
        this.dataSaver = new DataSaver();
    }

    public void initialize() {
        try {
            Files.createDirectories(tablesDirectory);
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать необходимые директории: " + tablesDirectory + " или " + dataDirectory, e);
        }
    }

    public void loadAllTables() {
        try (Stream<Path> paths = Files.list(tablesDirectory)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(this::loadTableFromFile);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файлов схем в " + tablesDirectory + ": " + e.getMessage());
        }
    }

    private void loadTableFromFile(Path schemaFilePath) {
        Optional<TableSchema> schemaOpt = schemaLoader.loadSchema(schemaFilePath);
        if (schemaOpt.isPresent()) {
            TableSchema schema = schemaOpt.get();
            String tableName = schema.getTableName();
            Optional<DataTable> dataTableOpt = dataLoader.loadData(dataDirectory, schema);

            dataTableOpt.ifPresentOrElse(
                    dataTable -> tables.put(tableName, dataTable),
                    () -> System.err.println("Не удалось загрузить данные для таблицы: " + tableName)
            );
        } else {
            System.err.println("Не удалось загрузить схему из файла: " + schemaFilePath);
        }
    }

    public Optional<DataTable> findTable(String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }

    public Map<String, DataTable> getAllTables() {
        return Collections.unmodifiableMap(tables);
    }

    public boolean saveTable(String tableName) throws IOException {
        DataTable table = tables.get(tableName);
        if (table == null) {
            return false;
        }
        dataSaver.saveData(dataDirectory, table);
        return true;
    }

    public void saveAllTables() throws IOException {
        for (Map.Entry<String, DataTable> entry : tables.entrySet()) {
            dataSaver.saveData(dataDirectory, entry.getValue());
        }
    }

    public boolean createTable(TableSchema schema) throws IOException {
        String tableName = schema.getTableName();
        if (tables.containsKey(tableName)) {
            return false;
        }
        DataTable newTable = new DataTable(schema);
        tables.put(tableName, newTable);
        dataSaver.saveSchema(tablesDirectory, schema);
        dataSaver.saveData(dataDirectory, newTable);
        return true;
    }

    public boolean dropTable(String tableName) throws IOException {
        DataTable table = tables.remove(tableName);
        if (table == null) {
            return false;
        }
        Path schemaPath = tablesDirectory.resolve(tableName + ".json");
        Path dataPath = dataDirectory.resolve(tableName + ".json");
        Files.deleteIfExists(schemaPath);
        Files.deleteIfExists(dataPath);
        return true;
    }
}
