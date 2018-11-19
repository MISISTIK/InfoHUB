package itea.project.controllers;

import itea.project.model.DataRow;
import itea.project.model.TableData;
import itea.project.utils.Ini4J;
import itea.project.utils.SQLThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static itea.project.utils.FxUtils.alertError;
import static itea.project.utils.FxUtils.alertInfo;
import static itea.project.utils.Utils.*;


public class SupplierController extends Controller {

    @FXML
    private Button export2ExcelButton;

    @FXML
    private Button suppBtn;

    @FXML
    private TableView<DataRow> table;

    @FXML
    private TextField suppInputField;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label timeLabel;

    @FXML
    private TabPane tabPane;


    private Ini4J ini;

    @FXML
    private void initialize() {
        ini = Ini4J.getInstance();
        TableData tdInfo = new TableData("id","SUPPLIER_NUM","SUPPLIER_NAME","EMAIL","PHONE","STATUS","EDRPOU","GLN");
        TableData tdArticle = new TableData("ART_SUPPLIER","id","SEGMENT","ART_NUM","ART_NAME","STATUS");
        setCurrentTable(tdInfo);

        suppInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
            {
                suppBtn.fire();
                event.consume();
            }
            suppInputField.setStyle("-fx-control-inner-background: white");
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            switch (tabPane.getSelectionModel().getSelectedIndex()) {
                case 0: {
                    setCurrentTable(tdInfo);
                    break;
                }
                case 1: {
                    setCurrentTable(tdArticle);
                    break;
                }
                default: {
                    break;
                }
            }
        });

        export2ExcelButton.setOnAction((event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save to Excel");
            fileChooser.setInitialFileName("Supplier.xlsx");
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
                    excelMap.put("SupplierArticles", tdArticle.getDataForExcel());
                    excelMap.put("Info", tdInfo.getDataForExcel());

                    save2Excel(file_xls.getAbsolutePath(), excelMap);

                    alertInfo("File \"" + filename + "\" successfully created", "Congrats !");
                }
            } catch (Exception e) {
                alertError(e);
            }
        });

        suppBtn.setOnAction((event) -> {
            try {
                tdInfo.clear();
                tdArticle.clear();
                String supp_str = suppInputField.getText();
                if (supp_str.matches("[\\d]+")) {

                    String StoreUrl = ini.getParam("CONNECTIONS", "StoreUrl");
                    List<SQLThread> threadPool = new ArrayList<>();
                    threadPool.add(new SQLThread(MessageFormat.format(getSQLFromFile("SupplierInfo_supp.sql"), supp_str),
                            StoreUrl, tdInfo, "SupplierInfo_Thread"));

                    threadPool.add(new SQLThread(MessageFormat.format(getSQLFromFile("SupplierArts.sql"), supp_str),
                            StoreUrl, tdArticle, "SupplierArts_Thread"));

                    new Thread(() -> {
                        long startTime = System.currentTimeMillis();
                        Platform.runLater(() -> {
                            timeLabel.setText("");
                            suppBtn.setDisable(true);
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
                            suppInputField.setStyle("-fx-control-inner-background: lime");
                            if (tdInfo.getTableData().size() == 0) {
                                suppInputField.setStyle("-fx-control-inner-background: yellow");
                            }
                            long stopTime = System.currentTimeMillis();
                            long elapsedTime = stopTime - startTime;
                            timeLabel.setText((String.format("%.2f sec", elapsedTime / 1000.0)));
                            suppBtn.setDisable(false);
                            export2ExcelButton.setDisable(false);
                            progressIndicator.setVisible(false);
                        });
                    }).start();
                } else {
                    Platform.runLater(() -> suppInputField.setStyle("-fx-control-inner-background: red"));
                }
                Platform.runLater(() -> {
                    suppInputField.setText(supp_str);
                    suppInputField.selectAll();
                });
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
        Platform.runLater(() -> suppInputField.requestFocus());
    }


}
