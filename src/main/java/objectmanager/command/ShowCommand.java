package objectmanager.command;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.ErrorResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.command.result.TableResult;
import objectmanager.model.DataObject;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.service.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Команда для отображения объектов в таблице Использует паттерн Decorator для
 * улучшения вывода данных
 */
public class ShowCommand extends AbstractCommand {

    public ShowCommand() {
        super("show", "Показывает все объекты в таблице", "show <имя_таблицы>");
    }

    @Override
    protected CommandResult executeCommand(DatabaseManager dbManager, List<String> args) {
        String tableName = args.get(0);
        Optional<DataTable> tableOpt = dbManager.getTable(tableName);

        if (tableOpt.isEmpty()) {
            return new ErrorResult("Таблица '" + tableName + "' не найдена.");
        }

        DataTable table = tableOpt.get();
        TableSchema schema = table.getSchema();
        List<DataObject> objects = table.getAllObjects();

        if (objects.isEmpty()) {
            return new SuccessResult("Таблица '" + tableName + "' не содержит объектов.");
        }

        // Получаем заголовки полей из схемы
        List<String> fieldNames = schema.getFieldNames();

        // Строим таблицу результатов
        TableResult.Builder resultBuilder = new TableResult.Builder()
                .withTitle("Объекты таблицы: " + tableName)
                .withHeaders(fieldNames);

        // Добавляем строки с объектами
        for (DataObject obj : objects) {
            List<String> rowData = new ArrayList<>();

            for (String fieldName : fieldNames) {
                String value = obj.getFieldValue(fieldName).orElse("<нет>");
                rowData.add(value);
            }

            resultBuilder.addRow(rowData);
        }

        resultBuilder.withFooter("Всего объектов: " + objects.size());

        return resultBuilder.build();
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() == 1;
    }
}
