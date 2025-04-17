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

    public TableSchema getSchema() {
        return schema;
    }

    public void addDataObject(DataObject dataObject) {
        dataObjects.add(dataObject);
    }

    public List<DataObject> getAllObjects() {
        return Collections.unmodifiableList(dataObjects);
    }

    public List<DataObject> getDataObjects() {
        return getAllObjects();
    }

    public int getObjectCount() {
        return dataObjects.size();
    }

    public int getRowCount() {
        return getObjectCount();
    }

}
