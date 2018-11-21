package itea.project.controllers;

import itea.project.model.DataRow;
import itea.project.model.TableData;
import itea.project.utils.Ini4J;
import itea.project.utils.SQLThread;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static itea.project.utils.FxUtils.alertError;
import static itea.project.utils.FxUtils.alertInfo;
import static itea.project.utils.Utils.getSQLFromFile;
import static itea.project.utils.Utils.save2Excel;


public class MultibaseController extends Controller {

    @FXML
    private Button export2ExcelButton;

    @FXML
    private Button searchBtn;

    @FXML
    private TableView<DataRow> table;

    @FXML
    private TextField inputField;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label timeLabel;

    @FXML
    private TabPane tabPane;

    private Ini4J ini;
    private ChangeListener tabListener = null;
    private Semaphore semaphore = new Semaphore(1);

    public TabPane getTabPane() {
        return tabPane;
    }

    @FXML
    private void initialize() {
        ini = Ini4J.getInstance();
        TableData tdAll = new TableData();
        TableData tdMySql = new TableData();
        TableData tdOracle = new TableData();
        TableData tdSqlite = new TableData();
        setCurrentTable(tdAll);

        tabListener = (observable, oldValue, newValue) -> {
            switch (tabPane.getSelectionModel().getSelectedIndex()) {
                case 0: {
                    setCurrentTable(tdAll);
                    break;
                }
                case 1: {
                    setCurrentTable(tdMySql);
                    break;
                }
                case 2: {
                    setCurrentTable(tdOracle);
                    break;
                }
                case 3: {
                    setCurrentTable(tdSqlite);
                    break;
                }

                default: {
                    alertError(new Exception("Now such tab with that index " + tabPane.getSelectionModel().getSelectedIndex()));
                    break;
                }
            }
        };

        tdAll.setChangeListener(tabListener);
        tdMySql.setChangeListener(tabListener);
        tdMySql.setChangeListener(tabListener);
        tdOracle.setChangeListener(tabListener);
        tdSqlite.setChangeListener(tabListener);

        tabPane.getSelectionModel().selectedItemProperty().addListener(tabListener);

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchBtn.fire();
                event.consume();
            }
            inputField.setStyle("-fx-control-inner-background: white");
        });

        export2ExcelButton.setOnAction((event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save to Excel");
            fileChooser.setInitialFileName(this.getClass().getSimpleName() + ".xlsx");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Excel", "*.xlsx"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            fileChooser.setInitialDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
            try {

                File file_xls = fileChooser.showSaveDialog(null);

                if (file_xls != null) {
                    String filename = file_xls.getName();
                    if (filename.length() < 5 || !filename.substring(filename.length() - 5, filename.length()).equals(".xlsx")) {
                        filename += ".xlsx";
                        file_xls = new File(file_xls.getParent() + "/" + filename);
                    }
                    Map<String, List<DataRow>> excelMap = new HashMap<>();
                    excelMap.put("Sqlite", tdSqlite.getDataForExcel());
                    excelMap.put("Oracle", tdOracle.getDataForExcel());
                    excelMap.put("MySql", tdMySql.getDataForExcel());
                    excelMap.put("All", tdAll.getDataForExcel());

                    save2Excel(file_xls.getAbsolutePath(), excelMap);

                    alertInfo("File \"" + filename + "\" successfully created", "Congrats !");
                }
            } catch (Exception e) {
                alertError(e);
            }
        });

        searchBtn.setOnAction((event) -> {
            try {
                tdAll.clear();
                tdMySql.clear();
                tdOracle.clear();
                tdSqlite.clear();
                List<SQLThread> threadPool = new ArrayList<>();
                // Connections block
                String OracleUrl = ini.getParam("CONNECTIONS", "OracleUrl");
                String OracleName = ini.getParam("CONNECTIONS", "OracleName");
                String OraclePassword = ini.getParam("CONNECTIONS", "OraclePassword");
                String MySqlUrl = ini.getParam("CONNECTIONS", "MySqlUrl");
                String MySqlName = ini.getParam("CONNECTIONS", "MySqlName");
                String MySqlPassword = ini.getParam("CONNECTIONS", "MySqlPassword");
                String SqliteUrl = ini.getParam("CONNECTIONS", "SqliteUrl");


                if (MySqlUrl.length() != 0 && MySqlName.length() != 0 && MySqlPassword.length() != 0) {
                    threadPool.add(new SQLThread(
                            getSQLFromFile("1_MySql.sql"),
                            MySqlUrl,
                            MySqlName,
                            MySqlPassword,
                            tdMySql,
                            semaphore,
                            "MySQL_Thread"));
                    threadPool.add(new SQLThread(
                            getSQLFromFile("1_MySql.sql"),
                            MySqlUrl,
                            MySqlName,
                            MySqlPassword,
                            tdAll,
                            semaphore,
                            "MySQL_Thread"));
                }

                if (OracleUrl.length() != 0 && OracleName.length() != 0 && OraclePassword.length() != 0) {
                    threadPool.add(new SQLThread(
                            getSQLFromFile("2_OracleSQL.sql"),
                            OracleUrl,
                            OracleName,
                            OraclePassword,
                            tdOracle,
                            semaphore,
                            "Oracle_Thread"));
                    threadPool.add(new SQLThread(
                            getSQLFromFile("2_OracleSQL.sql"),
                            OracleUrl,
                            OracleName,
                            OraclePassword,
                            tdAll,
                            semaphore,
                            "Oracle_Thread"));
                }
                if (SqliteUrl.length() != 0) {
                    threadPool.add(new SQLThread(
                            getSQLFromFile("3_SqliteSQL.sql"),
                            SqliteUrl,
                            tdSqlite,
                            semaphore,
                            "Sqlite_Thread"));

                    threadPool.add(new SQLThread(
                            getSQLFromFile("3_SqliteSQL.sql"),
                            SqliteUrl,
                            tdAll,
                            semaphore,
                            "Sqlite_Thread"));
                }
                new Thread(() -> {
                    long startTime = System.currentTimeMillis();
                    Platform.runLater(() -> {
                        timeLabel.setText("");
                        searchBtn.setDisable(true);
                        export2ExcelButton.setDisable(true);
                        progressIndicator.setVisible(true);
                    });

                    for (SQLThread sql_t : threadPool) {
                        sql_t.start();
                        if (!Boolean.parseBoolean(ini.getParam("ROOT", "UseParallelThreads"))) {
                            sql_t.join();
                        }
                    }
                    if (Boolean.parseBoolean(ini.getParam("ROOT", "UseParallelThreads"))) {
                        for (SQLThread sql_t : threadPool) {
                            sql_t.join();
                        }
                    }
                    Platform.runLater(() -> {
                        inputField.setStyle("-fx-control-inner-background: lime");
                        if (tdAll.getTableData().size() == 0) {
                            inputField.setStyle("-fx-control-inner-background: yellow");
                        }
                        long stopTime = System.currentTimeMillis();
                        long elapsedTime = stopTime - startTime;
                        timeLabel.setText((String.format("%.2f sec", elapsedTime / 1000.0)));
                        searchBtn.setDisable(false);
                        export2ExcelButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                }).start();
            } catch (Exception e) {
                alertError(e);
            }
        });
    }

    private void setCurrentTable(TableData tableDataToShow) {
        table.getColumns().setAll(tableDataToShow.getTableColumns());
        table.setItems(tableDataToShow.getTableData());
    }

    @Override
    public void setFocus() {
        Platform.runLater(() -> inputField.requestFocus());
    }


}
