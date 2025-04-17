package objectmanager.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import objectmanager.command.result.CommandResult;
import objectmanager.command.result.ErrorResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.model.DataObject;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.service.DatabaseManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Команда для добавления новых объектов в таблицу Использует паттерн Builder
 * для построения объекта из JSON
 */
public class InsertCommand extends AbstractCommand {

    public InsertCommand() {
        super("insert", "Добавляет новый объект в таблицу", "insert <имя_таблицы> <json_объект>");
    }

    @Override
    protected CommandResult executeCommand(DatabaseManager dbManager, List<String> args) {
        String tableName = args.get(0);
        String jsonData = String.join(" ", args.subList(1, args.size()));

        Optional<DataTable> tableOpt = dbManager.getTable(tableName);
        if (tableOpt.isEmpty()) {
            return new ErrorResult("Таблица '" + tableName + "' не найдена.");
        }

        DataTable table = tableOpt.get();
        TableSchema schema = table.getSchema();

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

            Set<String> requiredFields = schema.getFieldNames().stream()
                    .collect(Collectors.toSet());

            Set<String> jsonFields = jsonObject.keySet();

            for (String field : jsonFields) {
                if (!requiredFields.contains(field)) {
                    return new ErrorResult("Поле '" + field + "' не существует в схеме таблицы '" + tableName + "'.");
                }
            }

            DataObject dataObject = new DataObject();

            for (String fieldName : requiredFields) {
                if (jsonObject.has(fieldName) && !jsonObject.get(fieldName).isJsonNull()) {
                    String value = jsonObject.get(fieldName).getAsString();
                    dataObject.setValue(fieldName, value);
                } else {
                    dataObject.setValue(fieldName, null);
                }
            }

            table.addDataObject(dataObject);

            dbManager.saveTable(tableName);

            return new SuccessResult("Объект успешно добавлен в таблицу '" + tableName + "'.");
        } catch (Exception e) {
            return new ErrorResult("Ошибка при добавлении объекта: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() >= 2;
    }
}
