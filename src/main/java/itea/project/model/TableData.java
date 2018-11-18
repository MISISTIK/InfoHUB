package itea.project.model;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static itea.project.utils.FxUtils.alertError;

public class TableData {

    private Semaphore semaphore = new Semaphore(1);
    private TableView<DataRow> table;

    private ObservableList<DataRow> dataList = FXCollections.observableArrayList();
    private List<String> headers;

    public TableData(TableView<DataRow> tableView) {
        this.table = tableView;
        table.setItems(dataList);

    }

    public void setHeaders(List<String> headers) {
        if (table != null) {
            this.headers = new ArrayList<>(headers);
            for (int i = 0; i < headers.size(); i++) {
                TableColumn<DataRow, String> col = new TableColumn<>(headers.get(i));
                int finalI = i;
                col.setCellValueFactory(cellData -> {
                    DataRow d = cellData.getValue();
                    return new ReadOnlyObjectWrapper<>(d.get(finalI)).asString();
                });
                table.getColumns().add(col);
            }
        } else {
            alertError(new Exception("Cannot set headers until the table instance is not set or table is empty"));
        }

    }

    public synchronized void addList(List<DataRow> list) {
        try {
            semaphore.acquire();
            dataList.addAll(list);
        } catch (InterruptedException e) {
            alertError(e);
        } finally {
            semaphore.release();
        }
    }

    public synchronized void addRow(Object... objs) {
        try {
            semaphore.acquire();
            dataList.add(new DataRow(objs));
        } catch (InterruptedException e) {
            alertError(e);
        } finally {
            semaphore.release();
        }
    }
}
