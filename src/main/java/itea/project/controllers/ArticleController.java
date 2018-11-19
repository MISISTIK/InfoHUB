package itea.project.controllers;

import itea.project.model.DataRow;
import itea.project.model.TableData;
import itea.project.utils.Ini4J;
import itea.project.utils.SQLThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static itea.project.utils.Utils.getSQLFromFile;
import static itea.project.utils.Utils.getStoreList;


public class ArticleController extends Controller {

    @FXML
    private Button btn;

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


    @FXML
    private void initialize() {
        ini = Ini4J.getInstance();
        TableData tdInfo = new TableData("id", "SEGMENT", "ART_NUM", "ART_NAME", "ART_SUPPLIER", "STATUS");
        TableData tdPrice = new TableData("STORE_NUM", "id", "ART_NUM", "ART_PURCH_PRICE", "ART_PRICE");
        TableData tdSuppliers = new TableData("id","SUPPLIER_NUM","SUPPLIER_NAME","EMAIL","PHONE","STATUS","EDRPOU","GLN");
        setCurrentTable(tdInfo);

        inputField.setOnKeyTyped(event -> inputField.setStyle("-fx-control-inner-background: white"));

        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            switch (tabPane.getSelectionModel().getSelectedIndex()) {
                case 0: {
                    setCurrentTable(tdInfo);
                    break;
                }
                case 1: {
                    setCurrentTable(tdPrice);
                    break;
                }
                case 2: {
                    setCurrentTable(tdSuppliers);
                    break;
                }
                default: {
                    break;
                }
            }
        });

        btn.setOnAction((event) -> {
            tdInfo.clear();
            tdPrice.clear();
            tdSuppliers.clear();
            String art_str = inputField.getText();
            if (art_str.matches("[\\d]+")) {
                String[] stores = getStoreList();

                String StoreUrl = ini.getParam("CONNECTIONS", "StoreUrl");
                List<SQLThread> threadPool = new ArrayList<>();
                threadPool.add(new SQLThread(MessageFormat.format(getSQLFromFile("ArticleInfo.sql"), art_str),
                        StoreUrl, tdInfo, "ArtInfo_Thread"));

                for (String s : stores) {
                    threadPool.add(new SQLThread(
                            MessageFormat.format(getSQLFromFile("PriceInfo.sql"), art_str, s),
                            StoreUrl, tdPrice, "PriceInfo_Thread_" + s
                    ));
                }

                threadPool.add(new SQLThread(MessageFormat.format(getSQLFromFile("SupplierInfo.sql"), art_str),
                        StoreUrl, tdSuppliers, "SuppliersInfo_Thread"));


                new Thread(() -> {
                    Platform.runLater(() -> {
                        timeLabel.setText("");
                        btn.setDisable(true);
                        progressIndicator.setVisible(true);
                    });
                    long startTime = System.currentTimeMillis();
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
                        progressIndicator.setVisible(false);
                    });


                }).start();


                tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedItem());
            } else {
                Platform.runLater(() -> inputField.setStyle("-fx-control-inner-background: red"));
            }
            Platform.runLater(() -> {
                inputField.setText(art_str);
                inputField.selectAll();
            });

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
