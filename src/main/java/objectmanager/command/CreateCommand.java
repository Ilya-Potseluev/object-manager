package objectmanager.command;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.ErrorResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.model.TableSchema;
import objectmanager.repository.TableRepository;

/**
 * Команда для создания новой таблицы Использует паттерн Factory для создания
 * объекта схемы
 */
public class CreateCommand extends AbstractCommand {

    public CreateCommand() {
        super("create", "Создает новую таблицу на основе JSON-схемы",
                "create <имя_таблицы> <json_схема>");
    }

    @Override
    protected CommandResult executeCommand(TableRepository tableRepository, List<String> args) {
        String tableName = args.get(0);
        String jsonSchema = String.join(" ", args.subList(1, args.size()));

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonSchema).getAsJsonObject();

            if (!jsonObject.has("fields") || !jsonObject.get("fields").isJsonObject()) {
                return new ErrorResult("Ошибка в схеме: отсутствует поле 'fields' или оно не является объектом.");
            }

            JsonObject fieldsObject = jsonObject.getAsJsonObject("fields");
            Map<String, String> fields = new HashMap<>();

            fieldsObject.entrySet().forEach(entry -> {
                fields.put(entry.getKey(), entry.getValue().getAsString());
            });

            if (fields.isEmpty()) {
                return new ErrorResult("Ошибка в схеме: таблица должна содержать хотя бы одно поле.");
            }

            TableSchema schema = new TableSchema();
            schema.setTableName(tableName);
            schema.setFields(fields);

            if (jsonObject.has("description") && jsonObject.get("description").isJsonPrimitive()) {
                schema.setDescription(jsonObject.get("description").getAsString());
            } else {
                schema.setDescription("Таблица " + tableName);
            }

            boolean created = tableRepository.createTable(schema);

            if (created) {
                return new SuccessResult("Таблица '" + tableName + "' успешно создана.");
            } else {
                return new ErrorResult("Таблица '" + tableName + "' уже существует.");
            }

        } catch (IOException e) {
            return new ErrorResult("Ошибка при создании таблицы: " + e.getMessage(), e);
        } catch (Exception e) {
            return new ErrorResult("Ошибка при разборе JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() >= 2;
    }
}
