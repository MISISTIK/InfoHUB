package itea.project.controllers;

import itea.project.MainApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import sun.rmi.runtime.Log;

import static itea.project.MainApp.LOGGER;

public class RootController extends Controller {

    @FXML
    private BorderPane bp;

    @FXML
    private Menu mainMenu;
    @FXML
    private Label articleMenuLabel;
    @FXML
    private Label supplierMenuLabel;


    @FXML
    private RadioMenuItem art;
    @FXML
    private RadioMenuItem suppl;

    @FXML
    private void initialize() {

        Platform.runLater(() -> {
            ImageView menuPic = new ImageView("/img/menu.png");
            menuPic.setFitHeight(25);
            menuPic.setFitWidth(25);
            mainMenu.setGraphic(menuPic);
        });
//------------ setting the ini parameters on click -----------


    }
}
