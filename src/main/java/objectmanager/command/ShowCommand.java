package objectmanager.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.ErrorResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.command.result.TableResult;
import objectmanager.model.DataObject;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.repository.TableRepository;

/**
 * Команда для отображения объектов в таблице Использует паттерн Decorator для
 * улучшения вывода данных
 */
public class ShowCommand extends AbstractCommand {

    public ShowCommand() {
        super("show", "Показывает содержимое таблицы", "show <имя_таблицы>");
    }

    @Override
    protected CommandResult executeCommand(TableRepository tableRepository, List<String> args) {
        String tableName = args.get(0);

        Optional<DataTable> tableOpt = tableRepository.findTable(tableName);
        if (tableOpt.isEmpty()) {
            return new ErrorResult("Таблица не найдена: " + tableName);
        }

        DataTable table = tableOpt.get();
        TableSchema schema = table.getSchema();
        List<DataObject> objects = table.getDataObjects();

        if (objects.isEmpty()) {
            return new SuccessResult("Таблица '" + tableName + "' пуста.");
        }

        TableResult.Builder resultBuilder = new TableResult.Builder()
            .withTitle("Содержимое таблицы '" + tableName + "'")
            .withHeaders(schema.getFieldNames());
        
        for (DataObject obj : objects) {
            List<String> row = new ArrayList<>();
            for (String field : schema.getFieldNames()) {
                String value = obj.getValue(field);
                row.add(value == null ? "" : value);
            }
            resultBuilder.addRow(row);
        }
        
        String footer = "Всего объектов: " + objects.size();
        return resultBuilder.withFooter(footer).build();
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() == 1;
    }
}
