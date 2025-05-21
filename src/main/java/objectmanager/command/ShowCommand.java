package objectmanager.command;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import objectmanager.command.result.CommandResult;
import objectmanager.command.result.ErrorResult;
import objectmanager.command.result.SuccessResult;
import objectmanager.model.DataTable;
import objectmanager.model.TableSchema;
import objectmanager.repository.TableRepository;

/**
 * Команда для отображения схемы таблицы
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ShowCommand extends AbstractCommand {

    public ShowCommand() {
        super("show", "Отображает схему таблицы", "show <имя_таблицы>");
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

        StringBuilder result = new StringBuilder();
        result.append("Схема таблицы: ").append(tableName).append("\n");
        result.append("Описание: ").append(schema.getDescription()).append("\n\n");
        result.append("Поля:\n");

        Map<String, String> fields = schema.getFields();
        for (Map.Entry<String, String> field : fields.entrySet()) {
            result.append("  ").append(field.getKey())
                    .append(" (").append(field.getValue()).append(")\n");
        }

        result.append("\nКоличество объектов: ").append(table.getObjectCount());

        return new SuccessResult(result.toString());
    }

    @Override
    public boolean validateArgs(List<String> args) {
        return args.size() == 1;
    }
}
