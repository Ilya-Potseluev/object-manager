package objectmanager.command.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Результат в виде таблицы (паттерн Formatter/Template)
 */
public class TableResult implements CommandResult {

    private final String title;
    private final List<String> headers;
    private final List<List<String>> rows;
    private final String footer;

    private TableResult(Builder builder) {
        this.title = builder.title;
        this.headers = builder.headers;
        this.rows = builder.rows;
        this.footer = builder.footer;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String getMessage() {
        return title;
    }

    @Override
    public String format() {
        if (rows.isEmpty()) {
            return "Нет данных для отображения.";
        }

        StringBuilder sb = new StringBuilder();

        if (title != null && !title.isEmpty()) {
            sb.append(title).append('\n');
            sb.append("=".repeat(Math.max(50, title.length()))).append('\n');
        }

        int[] columnWidths = calculateColumnWidths();

        if (!headers.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                sb.append(String.format("%-" + columnWidths[i] + "s | ", headers.get(i)));
            }
            sb.append('\n');

            for (int width : columnWidths) {
                sb.append("-".repeat(width)).append("-+-");
            }
            sb.append('\n');
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                sb.append(String.format("%-" + columnWidths[i] + "s | ", row.get(i)));
            }
            sb.append('\n');
        }

        if (footer != null && !footer.isEmpty()) {
            sb.append("=".repeat(Math.max(50, title != null ? title.length() : 0))).append('\n');
            sb.append(footer);
        }

        return sb.toString();
    }

    private int[] calculateColumnWidths() {
        int columnCount = headers.size();
        int[] widths = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            widths[i] = headers.get(i).length();
        }

        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(columnCount, row.size()); i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }

        for (int i = 0; i < columnCount; i++) {
            widths[i] = Math.max(widths[i], 10);
        }

        return widths;
    }

    /**
     * Строитель для TableResult (паттерн Builder)
     */
    public static class Builder {

        private String title;
        private List<String> headers = new ArrayList<>();
        private List<List<String>> rows = new ArrayList<>();
        private String footer;

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withHeaders(List<String> headers) {
            this.headers = new ArrayList<>(headers);
            return this;
        }

        public Builder addRow(List<String> row) {
            this.rows.add(new ArrayList<>(row));
            return this;
        }

        public Builder withFooter(String footer) {
            this.footer = footer;
            return this;
        }

        public TableResult build() {
            return new TableResult(this);
        }
    }
}
