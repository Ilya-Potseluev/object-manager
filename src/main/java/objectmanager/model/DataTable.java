package objectmanager.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataTable {

    private final TableSchema schema;
    private final List<DataObject> dataObjects = new ArrayList<>();

    public DataTable(TableSchema schema) {
        this.schema = schema;
    }

    public void addDataObject(DataObject dataObject) {
        dataObjects.add(dataObject);
    }

    public TableSchema getSchema() {
        return schema;
    }

    public List<DataObject> getDataObjects() {
        return Collections.unmodifiableList(dataObjects);
    }

    public int getObjectCount() {
        return dataObjects.size();
    }

    public int getRowCount() {
        return getObjectCount();
    }

}
