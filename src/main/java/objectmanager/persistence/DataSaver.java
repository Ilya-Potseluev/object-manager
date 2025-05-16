package objectmanager.persistence;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import objectmanager.model.DataObject;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;

/**
 * Сохраняет данные и схемы в JSON файлы
 */
@Component
public class DataSaver {

    private final Gson gson;

    public DataSaver() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveSchema(Path tablesDirectory, TableSchema schema) throws IOException {
        String tableName = schema.getTableName();
        Path schemaFilePath = tablesDirectory.resolve(tableName + ".json");

        try (Writer writer = new FileWriter(schemaFilePath.toFile())) {
            JsonObject schemaJson = new JsonObject();
            schemaJson.addProperty("description", schema.getDescription());

            JsonObject fieldsJson = new JsonObject();
            for (String fieldName : schema.getFieldNames()) {
                fieldsJson.addProperty(fieldName, schema.getFieldType(fieldName));
            }
            schemaJson.add("fields", fieldsJson);

            gson.toJson(schemaJson, writer);
        }
    }

    public void saveData(Path dataDirectory, DataTable table) throws IOException {
        String tableName = table.getSchema().getTableName();
        Path dataFilePath = dataDirectory.resolve(tableName + ".json");

        try (Writer writer = new FileWriter(dataFilePath.toFile())) {
            List<DataObject> objects = table.getDataObjects();
            JsonArray dataArray = new JsonArray();

            for (DataObject obj : objects) {
                JsonObject jsonObj = new JsonObject();
                for (String fieldName : table.getSchema().getFieldNames()) {
                    String value = obj.getValue(fieldName);
                    jsonObj.addProperty(fieldName, value);
                }
                dataArray.add(jsonObj);
            }

            gson.toJson(dataArray, writer);
        }
    }
}
