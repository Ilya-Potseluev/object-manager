package objectmanager.command;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.ErrorResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.model.DataObject;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.repository.TableRepository;

/**
 * Команда для добавления новых объектов в таблицу Использует паттерн Builder
 * для построения объекта из JSON
 */
public class InsertCommand extends AbstractCommand {

    public InsertCommand() {
        super("insert", "Добавляет новый объект в таблицу",
                "insert <имя_таблицы> <json_объект>");
    }

    @Override
    protected CommandResult executeCommand(TableRepository tableRepository, List<String> args) throws Exception {
        String tableName = args.get(0);
        String jsonStr = String.join(" ", args.subList(1, args.size()));

        Optional<DataTable> tableOpt = tableRepository.findTable(tableName);
        if (tableOpt.isEmpty()) {
            return new ErrorResult("Таблица не найдена: " + tableName);
        }

        DataTable table = tableOpt.get();
        TableSchema schema = table.getSchema();

        try {
            JsonObject jsonObj = JsonParser.parseString(jsonStr).getAsJsonObject();
            DataObject dataObject = new DataObject();

            for (String fieldName : schema.getFieldNames()) {
                if (jsonObj.has(fieldName) && !jsonObj.get(fieldName).isJsonNull()) {
                    dataObject.setValue(fieldName, jsonObj.get(fieldName).getAsString());
                } else {
                    dataObject.setValue(fieldName, null);
                }
            }

            table.addDataObject(dataObject);
            
            tableRepository.markTableAsDirty(tableName);
            
            return new SuccessResult("Объект успешно добавлен в таблицу '" + tableName + "'");

        } catch (Exception e) {
            return new ErrorResult("Ошибка при добавлении объекта: " + e.getMessage());
        }
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() >= 2;
    }
}
