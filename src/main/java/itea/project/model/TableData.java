package itea.project.model;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.xml.crypto.Data;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import static itea.project.utils.FxUtils.alertError;

public class TableData {

    private Semaphore semaphore = new Semaphore(1);
    private TableView<DataRow> table = new TableView<>();

    private ObservableList<DataRow> dataList = FXCollections.observableArrayList();
    private String[] headers;

    public TableData() {
        table.setItems(dataList);
    }

    public TableData(String ... headers) {
        this();
        this.setHeaders(headers);
    }

    public void setHeaders(String ... headers) {
        if (table != null) {
            if (!Arrays.equals(this.headers, headers)) {
                table.getColumns().setAll(Collections.emptyList());
                this.headers = headers;
                for (int i = 0; i < headers.length; i++) {
                    TableColumn<DataRow, String> col = new TableColumn<>(headers[i]);
                    int finalI = i;
                    col.setCellValueFactory(cellData -> {
                        DataRow d = cellData.getValue();
                        return new ReadOnlyObjectWrapper<>(d.get(finalI)).asString();
                    });
                    table.getColumns().add(col);
                }
            }
        } else {
            alertError(new Exception("Cannot set headers until the table instance is not set or table is empty"));
        }

    }

    public List<DataRow> getDataForExcel() {
        WeakReference<List<DataRow>> resList = new WeakReference<>(new ArrayList<>());
        resList.get().add(new DataRow(headers));
        resList.get().addAll(dataList);
        return resList.get();
    }


    public void clear() {
        dataList.clear();
    }

    public ObservableList<DataRow> getTableData() {
        return dataList;
    }
    public ObservableList<TableColumn<DataRow,?>> getTableColumns() {
        return table.getColumns();

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
