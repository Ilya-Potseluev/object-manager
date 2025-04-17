package objectmanager.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import objectmanager.model.TableSchema;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SchemaLoader {

    private final Gson gson = new Gson();

    public Optional<TableSchema> loadSchema(Path schemaFilePath) {
        if (!Files.exists(schemaFilePath) || !schemaFilePath.toString().endsWith(".json")) {
            System.err.println("Файл схемы не найден или не является JSON: " + schemaFilePath);
            return Optional.empty();
        }

        try (Reader reader = new FileReader(schemaFilePath.toFile())) {
            TableSchema schema = gson.fromJson(reader, TableSchema.class);

            if (schema == null || schema.getFields() == null) {
                System.err.println("Ошибка разбора файла схемы " + schemaFilePath + ": Неверный формат или отсутствует поле 'fields'.");
                return Optional.empty();
            }

            String fileName = schemaFilePath.getFileName().toString();
            String tableName = fileName.substring(0, fileName.lastIndexOf('.'));
            schema.setTableName(tableName);

            return Optional.of(schema);

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла схемы " + schemaFilePath + ": " + e.getMessage());
            return Optional.empty();
        } catch (JsonSyntaxException e) {
            System.err.println("Ошибка разбора JSON в файле схемы " + schemaFilePath + ": " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка при загрузке файла схемы " + schemaFilePath + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}
