package itea.project.controllers;

import itea.project.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.BorderPane;

public class SupplierController extends Controller {

    @FXML
    private BorderPane bp;

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

    private MainApp mainApp;


    @FXML
    private void initialize() {

    }

    public void setFocus() {
    }


}
