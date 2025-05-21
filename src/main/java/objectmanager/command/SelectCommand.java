package objectmanager.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.ErrorResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.command.result.TableResult;
import objectmanager.model.DataObject;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.repository.TableRepository;

/**
 * Команда для выбора данных из таблицы по условию
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectCommand extends AbstractCommand {

    public SelectCommand() {
        super("select", "Выбирает данные из таблицы по условию",
                "select <имя_таблицы> [where <поле> = <значение>]");
    }

    @Override
    protected CommandResult executeCommand(TableRepository tableRepository, List<String> args) {
        String tableName = args.get(0);

        boolean hasWhereClause = args.size() > 1 && args.get(1).equalsIgnoreCase("where");

        Optional<DataTable> table = tableRepository.findTable(tableName);
        if (table.isEmpty()) {
            return new ErrorResult("Таблица не найдена: " + tableName);
        }

        DataTable dataTable = table.get();
        TableSchema schema = dataTable.getSchema();

        List<DataObject> filteredObjects;

        if (hasWhereClause) {
            if (args.size() < 4) {
                return new ErrorResult("Неверный формат условия WHERE. Используйте: where <поле> = <значение>");
            }

            String fieldName = args.get(2);

            if (!args.get(3).equals("=")) {
                return new ErrorResult("Поддерживается только оператор '='.");
            }

            String value = String.join(" ", args.subList(4, args.size()));

            if (!schema.getFieldNames().contains(fieldName)) {
                return new ErrorResult("Поле не найдено: " + fieldName);
            }

            filteredObjects = dataTable.getDataObjects().parallelStream()
                    .filter(obj -> {
                        String fieldValue = obj.getValue(fieldName);
                        return fieldValue != null && fieldValue.equals(value);
                    })
                    .collect(Collectors.toList());
        } else {
            filteredObjects = dataTable.getDataObjects();
        }

        if (filteredObjects.isEmpty()) {
            return new SuccessResult("Не найдено объектов" + (hasWhereClause ? ", соответствующих условию." : "."));
        }

        String title = "Найдено " + filteredObjects.size() + " объектов"
                + (hasWhereClause ? " по условию" : "");

        TableResult.Builder resultBuilder = new TableResult.Builder()
                .withTitle(title)
                .withHeaders(schema.getFieldNames());

        for (DataObject obj : filteredObjects) {
            List<String> row = new ArrayList<>();
            for (String field : schema.getFieldNames()) {
                String value = obj.getValue(field);
                row.add(value == null ? "" : value);
            }
            resultBuilder.addRow(row);
        }

        String footer = hasWhereClause
                ? "Условие: " + args.get(2) + " = " + String.join(" ", args.subList(4, args.size()))
                : "Все объекты таблицы";

        return resultBuilder.withFooter(footer).build();
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() >= 1;
    }
}
