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
import java.util.function.Predicate;

/**
 * Команда для выборки объектов из таблицы с фильтрацией Использует паттерн
 * Strategy для разделения логики фильтрации
 */
public class SelectCommand extends AbstractCommand {

    public SelectCommand() {
        super("select", "Выбирает объекты из таблицы с опциональной фильтрацией",
                "select <имя_таблицы> [where <поле> <оператор> <значение>]");
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

        if (args.size() > 1 && "where".equalsIgnoreCase(args.get(1)) && args.size() >= 5) {
            String fieldName = args.get(2);
            String operator = args.get(3);
            String value = args.get(4);

            if (!schema.getFieldNames().contains(fieldName)) {
                return new ErrorResult("Поле '" + fieldName + "' не существует в схеме таблицы '" + tableName + "'.");
            }

            Predicate<DataObject> filter = createFilter(fieldName, operator, value);

            objects = objects.stream().filter(filter).toList();

            if (objects.isEmpty()) {
                return new SuccessResult("Нет объектов, соответствующих условию фильтрации.");
            }
        }

        List<String> fieldNames = schema.getFieldNames();

        TableResult.Builder resultBuilder = new TableResult.Builder()
                .withTitle("Результаты выборки из таблицы: " + tableName)
                .withHeaders(fieldNames);

        for (DataObject obj : objects) {
            List<String> rowData = new ArrayList<>();

            for (String fieldName : fieldNames) {
                String val = obj.getFieldValue(fieldName).orElse("<нет>");
                rowData.add(val);
            }

            resultBuilder.addRow(rowData);
        }

        resultBuilder.withFooter("Всего объектов: " + objects.size());

        return resultBuilder.build();
    }

    private Predicate<DataObject> createFilter(String fieldName, String operator, String value) {
        return obj -> {
            Optional<String> fieldValue = obj.getFieldValue(fieldName);

            if (fieldValue.isEmpty()) {
                return false;
            }

            String actualValue = fieldValue.get();

            return switch (operator.toLowerCase()) {
                case "=" ->
                    actualValue.equals(value);
                case "!=" ->
                    !actualValue.equals(value);
                case "contains" ->
                    actualValue.contains(value);
                case "startswith" ->
                    actualValue.startsWith(value);
                case "endswith" ->
                    actualValue.endsWith(value);
                default ->
                    false;
            };
        };
    }

    @Override
    public boolean validateArgs(List<String> args) {
        if (args.size() < 1) {
            return false;
        }

        if (args.size() > 1 && "where".equalsIgnoreCase(args.get(1))) {
            return args.size() >= 5;
        }

        return true;
    }
}
