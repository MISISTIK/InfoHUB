package itea.project.model;

public class DataRow {
    private Object[] data_array = new Object[0];

    public Object get(int i) {
        if (i > data_array.length-1) {
            return "No element in DataRow";
        }
        if (data_array[i] == null) {
            return "null";
        }
        return data_array[i];
    }

    public DataRow(Object... objs) {
        data_array = new Object[objs.length];
        for (int i = 0; i < objs.length; i++) {
            this.data_array[i] = objs[i].toString();
        }
    }
}