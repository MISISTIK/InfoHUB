package itea.project.controllers;

import itea.project.MainApp;
import itea.project.utils.Ini4J;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
    private CheckBox useParalel;

    @FXML
    private CheckBox useDelay;


    @FXML
    private Menu mainMenu;


    @FXML
    private void initialize() {

        Platform.runLater(() -> {
            ImageView menuPic = new ImageView("/img/menu.png");
            menuPic.setFitHeight(25);
            menuPic.setFitWidth(25);
            mainMenu.setGraphic(menuPic);
            useParalel.setSelected(Boolean.parseBoolean(Ini4J.getInstance().getParam("ROOT","UseParallelThreads")));
            useDelay.setSelected(Boolean.parseBoolean(Ini4J.getInstance().getParam("ROOT","UseDelay")));
        });

        useParalel.setOnAction(event -> {
            Ini4J.getInstance().setParam("ROOT","UseParallelThreads", String.valueOf(useParalel.isSelected()));
        });

        useDelay.setOnAction(event -> {
            Ini4J.getInstance().setParam("ROOT","UseDelay", String.valueOf(useDelay.isSelected()));
        });

//------------ setting the ini parameters on click -----------


    }
}
