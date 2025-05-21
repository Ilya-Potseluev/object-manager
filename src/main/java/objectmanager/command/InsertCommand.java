package objectmanager.command;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
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
 * Команда для вставки новых объектов в таблицу. Использует паттерн Builder для
 * конструирования объекта данных
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InsertCommand extends AbstractCommand {

    public InsertCommand() {
        super("insert", "Вставляет объект в таблицу", "insert <имя_таблицы> <json_данные>");
    }

    @Override
    protected CommandResult executeCommand(TableRepository tableRepository, List<String> args) {
        String tableName = args.get(0);
        String jsonData = String.join(" ", args.subList(1, args.size()));

        Optional<DataTable> table = tableRepository.findTable(tableName);
        if (table.isEmpty()) {
            return new ErrorResult("Таблица не найдена: " + tableName);
        }

        DataTable dataTable = table.get();
        TableSchema schema = dataTable.getSchema();

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            DataObject newObject = new DataObject();

            // Проверяем и добавляем поля
            for (String fieldName : schema.getFieldNames()) {
                JsonElement element = jsonObject.get(fieldName);
                if (element == null) {
                    return new ErrorResult("Отсутствует обязательное поле: " + fieldName);
                }
                newObject.setValue(fieldName, element.getAsString());
            }

            dataTable.addDataObject(newObject);
            tableRepository.markTableAsDirty(tableName);

            return new SuccessResult("Объект успешно добавлен в таблицу.");
        } catch (Exception e) {
            return new ErrorResult("Ошибка при добавлении объекта: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() >= 2;
    }
}
