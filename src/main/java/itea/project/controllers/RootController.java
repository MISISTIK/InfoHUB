package itea.project.controllers;

import itea.project.MainApp;
import itea.project.utils.Ini4J;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static itea.project.MainApp.LOGGER;
import static itea.project.utils.Utils.getResFileAsList;
import static itea.project.utils.Utils.listResFolder;

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

    @FXML
    private Button playButton;

    @FXML
    private Button nextButton;


    private Ini4J ini;
    private MediaPlayer player;
    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private int currentTrack = 0;
    List<String> mediaList = new ArrayList<>();

    private void mediaPlayerInit() {
        mediaList = listResFolder("media");
        if (mediaList.size() > 0) {
            String musicPath = MainApp.class.getResource("/" + mediaList.get(0)).toString();
            Media hit = new Media(musicPath);
            player = new MediaPlayer(hit);
            player.setStartTime(Duration.ZERO);
            player.setOnEndOfMedia(() -> {
                player.seek(Duration.ZERO);
                player.play();
            });
        }
    }

    @FXML
    private void initialize() {
        ini = Ini4J.getInstance();
        mediaPlayerInit();

        nextButton.setOnAction(event -> {
            if (player != null && mediaList.size() > 0) {
                if (currentTrack == mediaList.size() - 1) {
                    currentTrack = 0;
                }
                //player = new MediaPlayer();
            }
        });

        playButton.setOnAction(event -> {
            if (player != null) {
                if ((isPlaying.get())) {
                    playButton.setText("Play");
                    player.pause();
                } else {
                    playButton.setText("Stop");
                    player.play();
                }
                isPlaying.set(!isPlaying.get());
            }
        });

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
