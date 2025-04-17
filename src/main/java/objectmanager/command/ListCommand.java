package objectmanager.command;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.command.result.TableResult;
import objectmanager.model.DataTable;
import objectmanager.service.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Команда для отображения списка всех таблиц Использует паттерн Strategy для
 * форматирования вывода
 */
public class ListCommand extends AbstractCommand {

    public ListCommand() {
        super("list", "Показывает список всех таблиц", "list");
    }

    @Override
    protected CommandResult executeCommand(DatabaseManager dbManager, List<String> args) {
        Map<String, DataTable> tables = dbManager.getAllTables();

        if (tables.isEmpty()) {
            return new SuccessResult("В базе данных нет таблиц.");
        }

        TableResult.Builder resultBuilder = new TableResult.Builder()
                .withTitle("Таблицы в базе данных")
                .withHeaders(List.of("Имя таблицы", "Кол-во объектов", "Описание"));

        tables.forEach((name, table) -> {
            List<String> row = new ArrayList<>();
            row.add(name);
            row.add(String.valueOf(table.getObjectCount()));
            row.add(table.getSchema().getDescription());
            resultBuilder.addRow(row);
        });

        return resultBuilder.build();
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.isEmpty();
    }
}
