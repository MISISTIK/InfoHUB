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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static itea.project.utils.FxUtils.alertError;
import static itea.project.utils.FxUtils.alertInfo;
import static itea.project.utils.Utils.*;


public class ArticleController extends Controller {

    @FXML
    private Button export2ExcelButton;

    @FXML
    private Button btn;

    @FXML
    private Button testSearch;

    @FXML
    private TableView<DataRow> table;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField inputField;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label timeLabel;

    private Ini4J ini;
    private Semaphore semaphore = new Semaphore(1);
    private ChangeListener tabListener = null;

    public TabPane getTabPane() {
        return tabPane;
    }

    @FXML
    private void initialize() {
        ini = Ini4J.getInstance();
        TableData tdInfo = new TableData();
        TableData tdPrice = new TableData();
        TableData tdSupplier = new TableData();

        tabListener = (observable, oldValue, newValue) -> {
            switch (tabPane.getSelectionModel().getSelectedIndex()) {
                case 0: {
                    setCurrentTable(tdInfo);
                    break;
                }
                case 1: {
                    setCurrentTable(tdPrice);
                    break;
                }
                default: {
                    setCurrentTable(tdSupplier);
                    break;
                }
            }
        };

        tdInfo.setChangeListener(tabListener);
        tdPrice.setChangeListener(tabListener);
        tdSupplier.setChangeListener(tabListener);

        tabPane.getSelectionModel().selectedItemProperty().addListener(tabListener);

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
            {
                btn.fire();
                event.consume();
            }
            inputField.setStyle("-fx-control-inner-background: white");
        });

        export2ExcelButton.setOnAction((event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save to Excel");
            fileChooser.setInitialFileName("Article.xlsx");
            fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel", "*.xlsx"),
            new FileChooser.ExtensionFilter("All Files", "*.*"));
            fileChooser.setInitialDirectory(javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory());
            try {

                File file_xls = fileChooser.showSaveDialog(null);

                if (file_xls != null) {
                    String filename = file_xls.getName();
                    if (filename.length() < 5 || !filename.substring(filename.length() - 5, filename.length()).equals(".xlsx")) {
                        filename += ".xlsx";
                        file_xls = new File(file_xls.getParent() + "/" + filename);
                    }
                    Map<String, List<DataRow>> excelMap = new HashMap<>();
                    excelMap.put("Supplier", tdSupplier.getDataForExcel());
                    excelMap.put("Prices", tdPrice.getDataForExcel());
                    excelMap.put("Info", tdInfo.getDataForExcel());

                    if (save2Excel(file_xls.getAbsolutePath(), excelMap)) {
                        alertInfo("File \"" + filename + "\" successfully created", "Congrats !");
                    }
                }
            } catch (Exception e) {
                alertError(e);
            }
        });

        btn.setOnAction((event) -> {
            try {
                tdInfo.clear();
                tdPrice.clear();
                tdSupplier.clear();
                String art_str = inputField.getText();
                if (art_str.matches("[\\d]+")) {
                    String[] stores = getStoreList();

                    String StoreUrl = ini.getParam("CONNECTIONS", "StoreUrl");
                    List<SQLThread> threadPool = new ArrayList<>();

                    threadPool.add(new SQLThread(MessageFormat.format(getSQLFromFile("ArticleInfo.sql"), art_str),
                            StoreUrl, tdInfo,semaphore , "ArtInfo_Thread"));

                    for (String s : stores) {
                        threadPool.add(new SQLThread(
                                MessageFormat.format(getSQLFromFile("PriceInfo.sql"), art_str, s),
                                StoreUrl, tdPrice, semaphore, "PriceInfo_Thread_" + s
                        ));
                    }

                    threadPool.add(new SQLThread(MessageFormat.format(getSQLFromFile("SupplierInfo.sql"), art_str),
                            StoreUrl, tdSupplier, semaphore, "SuppliersInfo_Thread"));


                    new Thread(() -> {
                        long startTime = System.currentTimeMillis();
                        Platform.runLater(() -> {
                            timeLabel.setText("");
                            btn.setDisable(true);
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
                            if (tdInfo.getTableData().size() == 0) {
                                inputField.setStyle("-fx-control-inner-background: yellow");
                            }
                            long stopTime = System.currentTimeMillis();
                            long elapsedTime = stopTime - startTime;
                            timeLabel.setText((String.format("%.2f sec", elapsedTime / 1000.0)));
                            btn.setDisable(false);
                            export2ExcelButton.setDisable(false);
                            progressIndicator.setVisible(false);
                        });

                    }).start();
                } else {
                    Platform.runLater(() -> inputField.setStyle("-fx-control-inner-background: red"));
                }
                Platform.runLater(() -> {
                    inputField.setText(art_str);
                    inputField.selectAll();
                });
            } catch (Exception e) {
                alertError(e);
            }
        });
    }

    private synchronized void setCurrentTable(TableData tableDataToShow) {
        table.getColumns().setAll(tableDataToShow.getTableColumns());
        table.setItems(tableDataToShow.getTableData());
    }

    @Override
    public void setFocus() {
        Platform.runLater(() -> inputField.requestFocus());
    }


}
