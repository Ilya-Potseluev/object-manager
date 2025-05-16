package objectmanager.model;

import java.util.HashMap;
import java.util.Map;

public class DataObject {

    private final Map<String, String> values = new HashMap<>();

    public DataObject() {
    }

    public String getValue(String fieldName) {
        return values.get(fieldName);
    }

    public void setValue(String fieldName, String value) {
        values.put(fieldName, value);
    }

    public Map<String, String> getValues() {
        return new HashMap<>(values);
    }

}
