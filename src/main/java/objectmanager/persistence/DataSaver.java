package objectmanager.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import objectmanager.model.DataObject;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Класс для сохранения данных и схем в файловую систему Использует паттерн
 * Strategy для различных форматов сохранения
 */
public class DataSaver {

    private final Gson gson;

    public DataSaver() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public void saveSchema(Path directory, TableSchema schema) throws IOException {
        String tableName = schema.getTableName();
        Path schemaPath = directory.resolve(tableName + ".json");

        String schemaJson = gson.toJson(schema);
        Files.writeString(schemaPath, schemaJson);
    }

    public void saveData(Path directory, DataTable dataTable) throws IOException {
        String tableName = dataTable.getSchema().getTableName();
        Path dataPath = directory.resolve(tableName + ".json");

        List<Map<String, String>> dataToSave = new ArrayList<>();
        for (DataObject obj : dataTable.getAllObjects()) {
            dataToSave.add(obj.getValues());
        }

        String dataJson = gson.toJson(dataToSave);
        Files.writeString(dataPath, dataJson);
    }
}
