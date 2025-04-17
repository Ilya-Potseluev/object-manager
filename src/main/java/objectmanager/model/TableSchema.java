package objectmanager.model;

import java.util.List;
import java.util.Map;

public class TableSchema {

    private String tableName;
    private String description = "";
    private Map<String, String> fields;

    public TableSchema() {
    }

    public TableSchema(String tableName, Map<String, String> fields) {
        this(tableName, fields, "");
    }

    public TableSchema(String tableName, Map<String, String> fields, String description) {
        this.tableName = tableName;
        this.fields = fields;
        this.description = description;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public List<String> getFieldNames() {
        return List.copyOf(fields.keySet());
    }

}
