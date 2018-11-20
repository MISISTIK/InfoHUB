package itea.project.controllers;

import itea.project.MainApp;
import itea.project.utils.Ini4J;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.util.List;

import static itea.project.MainApp.LOGGER;
import static itea.project.utils.Utils.getResFileAsList;

public class RootController extends Controller {


    @FXML
    private Label articleMenu;

    @FXML
    private Label supplierMenu;

    @FXML
    private CheckBox useParalel;

    @FXML
    private CheckBox useDelay;

    @FXML
    private Menu mainMenu;

    @FXML
    private MenuItem menuItemAbout;

    @FXML
    private MenuItem menuItemClose;

    private Ini4J ini;

    @FXML
    private void initialize() {
        ini = Ini4J.getInstance();

        Platform.runLater(() -> {
            ImageView menuPic = new ImageView("/img/menu.png");
            menuPic.setFitHeight(25);
            menuPic.setFitWidth(25);
            mainMenu.setGraphic(menuPic);
            useParalel.setSelected(Boolean.parseBoolean(Ini4J.getInstance().getParam("ROOT", "UseParallelThreads")));
            useDelay.setSelected(Boolean.parseBoolean(Ini4J.getInstance().getParam("ROOT", "UseDelay")));
            switch (ini.getParam("ROOT","MenuOnStart")) {
                case "article": {
                    mainApp.setArticleLayout();
                    break;
                }
                default: {
                    mainApp.setSupplierLayout();
                    break;
                }

            }
        });

        menuItemClose.setOnAction((event -> {
            LOGGER.info("==================== PROGRAM END ====================");
            ini.setParam("ROOT", "PrefWidth", String.valueOf(mainApp.primaryStage.getWidth()).replace(".0", ""));
            ini.setParam("ROOT", "PrefHeight", String.valueOf(mainApp.primaryStage.getHeight()).replace(".0", ""));
            MainApp.isAppInit = false;
            Platform.exit();
        }));

        menuItemAbout.setOnAction((event ->
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("InfoHUB v1.1");
            alert.setHeaderText("About InfoHUB v1.1:");

            List<String> aboutStr = getResFileAsList("About.txt");

            String about = "";
            for (String s : aboutStr) {
                about = about + s + "\n";
            }
            about = about
                    .replace("[$javaVersion]", System.getProperty("java.version"))
                    .replace("[$javafxVersion]", System.getProperty("javafx.version"));

            alert.setContentText(about);
            alert.showAndWait();
        }));

        //------------ setting the ini parameters on click -----------
        useParalel.setOnAction(event ->
                Ini4J.getInstance().setParam("ROOT", "UseParallelThreads", String.valueOf(useParalel.isSelected())));

        useDelay.setOnAction(event ->
                Ini4J.getInstance().setParam("ROOT", "UseDelay", String.valueOf(useDelay.isSelected())));
        // ------------ setting the Layout change -------------
        articleMenu.setOnMouseClicked((event -> {
            ini.setParam("ROOT", "MenuOnStart", "article");
            mainApp.setArticleLayout();
        }));
        supplierMenu.setOnMouseClicked((event -> {
            ini.setParam("ROOT", "MenuOnStart", "supplier");
            mainApp.setSupplierLayout();
        }));
    }
}
