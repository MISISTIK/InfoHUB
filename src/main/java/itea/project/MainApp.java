package itea.project;

import itea.project.controllers.ArticleController;
import itea.project.controllers.Controller;
import itea.project.controllers.RootController;
import itea.project.utils.Ini4J;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import static itea.project.utils.FxUtils.getStackTrace;
import static itea.project.utils.Utils.checkSQLFolder;

public class MainApp extends Application {
    public static final Logger LOGGER = LogManager.getLogger();
    private BorderPane root = null;
    private AnchorPane articleLayout = null;
    private ArticleController articleController = null;

    private Stage primaryStage;
    private Ini4J ini;

    private void initLayout(boolean isDev) {
        ini = Ini4J.getInstance();
        checkSQLFolder();
        try {

            //for dev use
            if (isDev) {
                loadRootLayout(new URL("file://" + System.getProperty("user.dir") + "/src/main/java/itea/project/controllers/root.fxml"));
                loadArticleLayout(new URL("file://" + System.getProperty("user.dir") + "/src/main/java/itea/project/controllers/article.fxml"));
            } else {
                loadRootLayout(getClass().getResource("/view/root.fxml"));
                loadArticleLayout(getClass().getResource("/view/article.fxml"));
            }

            final int wVal = 10;
            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                root.setPrefWidth(primaryStage.getWidth() - wVal);
                articleLayout.setPrefWidth(primaryStage.getWidth() - wVal);

            });
            final int hVal = 85;
            primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
                root.setPrefHeight(primaryStage.getHeight() - hVal);
                articleLayout.setPrefHeight(primaryStage.getHeight() - hVal);

            });

            Platform.runLater(() -> {
                primaryStage.setMinWidth(Integer.valueOf(ini.getParam("ROOT","MinWidth")));
                primaryStage.setMinHeight(Integer.valueOf(ini.getParam("ROOT","MinHeight")));

                primaryStage.setWidth(Integer.valueOf(ini.getParam("ROOT","PrefWidth")));
                primaryStage.setHeight(Integer.valueOf(ini.getParam("ROOT","PrefHeight")));
                primaryStage.setHeight(primaryStage.getHeight()+1);
            });

            primaryStage.setTitle("InfoHUB");
            primaryStage.getIcons().add(new Image("/img/favicon.png"));
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(true);
            primaryStage.show();

            primaryStage.setOnCloseRequest(e -> {
                LOGGER.info("==================== PROGRAM END ====================");
                ini.setParam("ROOT", "PrefWidth", String.valueOf(primaryStage.getWidth()).replace(".0", ""));
                ini.setParam("ROOT", "PrefHeight", String.valueOf(primaryStage.getHeight()).replace(".0", ""));
            });
        } catch (Exception e) {
            LOGGER.error(getStackTrace(e));
            Platform.exit();
        }
    }

    private void loadArticleLayout(URL path) throws IOException {
        WeakReference<FXMLLoader> loader = new WeakReference<>(new FXMLLoader());
        loader.get().setLocation(path);
        articleLayout = loader.get().load();
        articleController = loader.get().getController();
        articleController.setMainApp(this);
        loader.clear();

    }

    private void loadRootLayout(URL path) throws IOException {
        WeakReference<FXMLLoader> loader = new WeakReference<>(new FXMLLoader());
        loader.get().setLocation(path);
        root = loader.get().load();
        root.setStyle("-fx-background-color: derive(#1d1d1d,20%)");
        WeakReference<RootController> rc = new WeakReference<>(loader.get().getController());
        rc.get().setMainApp(this);
        loader.clear();
    }

    public void setLayout(Node n, Controller c) {
        root.setCenter(n);
        c.setFocus();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LOGGER.info("=================== PROGRAM START ===================");
        this.primaryStage = primaryStage;
        //TODO change this for prod
        boolean dev = true;
        initLayout(dev);
        setLayout(articleLayout,articleController);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
