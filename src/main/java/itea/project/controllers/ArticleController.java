package itea.project.controllers;

import itea.project.MainApp;
import itea.project.model.DataRow;
import itea.project.model.TableData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleController extends Controller {

    @FXML
    private BorderPane borderPane;

    @FXML
    private Button btn;

    @FXML
    private TableView<DataRow> table;
    @FXML
    private Menu mainMenu;
    @FXML
    private Label articleMenuLabel;
    @FXML
    private Label supplierMenuLabel;
    @FXML
    private Label orderMenuLabel;

    @FXML
    private RadioMenuItem art;
    @FXML
    private RadioMenuItem suppl;

    @FXML
    private void initialize() {

        TableData td = new TableData(table);
        td.setHeaders(Arrays.asList("First","Second"));
    }

    @Override
    public void setFocus() {

    }
}
