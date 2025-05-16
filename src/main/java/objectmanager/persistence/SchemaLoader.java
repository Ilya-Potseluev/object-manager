package objectmanager.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import objectmanager.model.TableSchema;

/**
 * Загрузчик схем таблиц из файлов (часть паттерна Strategy)
 */
@Component
public class SchemaLoader {

    public Optional<TableSchema> loadSchema(Path schemaFilePath) {
        try {
            if (!Files.isRegularFile(schemaFilePath)) {
                System.err.println("Файл схемы не найден: " + schemaFilePath);
                return Optional.empty();
            }

            String jsonContent = Files.readString(schemaFilePath);
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            TableSchema schema = new TableSchema();

            String fileName = schemaFilePath.getFileName().toString();
            String tableName = fileName.substring(0, fileName.lastIndexOf('.'));
            schema.setTableName(tableName);

            if (jsonObject.has("description") && jsonObject.get("description").isJsonPrimitive()) {
                schema.setDescription(jsonObject.get("description").getAsString());
            } else {
                schema.setDescription("Таблица " + tableName);
            }

            Map<String, String> fields = new HashMap<>();
            if (jsonObject.has("fields") && jsonObject.get("fields").isJsonObject()) {
                JsonObject fieldsJson = jsonObject.getAsJsonObject("fields");
                for (Map.Entry<String, JsonElement> entry : fieldsJson.entrySet()) {
                    String fieldName = entry.getKey();
                    String fieldType = entry.getValue().getAsString();
                    fields.put(fieldName, fieldType);
                }
            }

            if (fields.isEmpty()) {
                System.err.println("Ошибка: Схема не содержит полей: " + schemaFilePath);
                return Optional.empty();
            }

            schema.setFields(fields);
            return Optional.of(schema);

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла схемы " + schemaFilePath + ": " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Ошибка при разборе схемы из " + schemaFilePath + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}
