package objectmanager.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import objectmanager.model.DataObject;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;

/**
 * Загрузчик данных из файлов
 */
@Component
public class TableLoader {

    public Optional<DataTable> loadData(Path dataDirectory, TableSchema schema) {
        String tableName = schema.getTableName();
        Path dataFilePath = dataDirectory.resolve(tableName + ".json");

        if (!Files.exists(dataFilePath) || !Files.isRegularFile(dataFilePath)) {
            return Optional.of(new DataTable(schema));
        }

        try {
            String jsonContent = Files.readString(dataFilePath);
            JsonArray jsonArray = JsonParser.parseString(jsonContent).getAsJsonArray();

            DataTable dataTable = new DataTable(schema);
            List<String> fieldNames = schema.getFieldNames();

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                DataObject dataObject = new DataObject();

                for (String fieldName : fieldNames) {
                    String value = null;
                    if (jsonObject.has(fieldName) && !jsonObject.get(fieldName).isJsonNull()) {
                        value = jsonObject.get(fieldName).getAsString();
                    }
                    dataObject.setValue(fieldName, value);
                }

                dataTable.addDataObject(dataObject);
            }

            return Optional.of(dataTable);

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла данных " + dataFilePath + ": " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Ошибка при разборе JSON из файла " + dataFilePath + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}
