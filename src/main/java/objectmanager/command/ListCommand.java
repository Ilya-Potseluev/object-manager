package objectmanager.command;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.command.result.TableResult;
import objectmanager.model.DataTable;
import objectmanager.repository.TableRepository;

/**
 * Команда для отображения списка всех таблиц Использует паттерн Strategy для
 * форматирования вывода
 */
public class ListCommand extends AbstractCommand {

    public ListCommand() {
        super("list", "Отображает список всех таблиц", "list");
    }

    @Override
    protected CommandResult executeCommand(TableRepository tableRepository, List<String> args) {
        Map<String, DataTable> tables = tableRepository.getAllTables();

        if (tables.isEmpty()) {
            return new SuccessResult("Таблицы не найдены.");
        }

        TableResult.Builder resultBuilder = new TableResult.Builder()
            .withTitle("Доступные таблицы")
            .withHeaders(Arrays.asList("Имя таблицы", "Кол-во объектов", "Описание"));
        
        for (Map.Entry<String, DataTable> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            DataTable table = entry.getValue();
            String description = table.getSchema().getDescription();
            int objectCount = table.getObjectCount();
            
            resultBuilder.addRow(Arrays.asList(
                tableName,
                String.valueOf(objectCount),
                description
            ));
        }
        
        return resultBuilder.withFooter("Всего таблиц: " + tables.size()).build();
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.isEmpty();
    }
}
