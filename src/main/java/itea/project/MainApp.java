package itea.project;

import itea.project.controllers.ArticleController;
import itea.project.controllers.Controller;
import itea.project.controllers.RootController;
import itea.project.controllers.SupplierController;
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

import static itea.project.utils.FxUtils.alertError;
import static itea.project.utils.FxUtils.getStackTrace;
import static itea.project.utils.Utils.*;

public class MainApp extends Application {
    public static Logger LOGGER = LogManager.getLogger();
    public static volatile boolean isAppInit;
    private BorderPane root = null;
    private AnchorPane articleLayout = null;
    private AnchorPane supplierLayout = null;
    private ArticleController articleController = null;
    private SupplierController supplierController = null;

    public Stage primaryStage;
    private Ini4J ini;


    //TODO Change dev to false
    private void initLayout(boolean isDev) {
        //extractJarResFolder(listResFolder("SQL_res"),"SQL");
        //extractJarResFolder(,"SQL");
        ini = Ini4J.getInstance();
        isAppInit = true;
        checkSQLFolder("SQL_res",ini.getParam("ROOT","SqlFolderName"));
        try {
            for (String driver : ini.getSectionValues("DRIVERS")) {
                Class.forName(driver);
            }
            //for dev use
            if (isDev) {
                String file_str = "file://" + (System.getProperty("os.name").contains("Windows") ? "/" : "");
                loadRootLayout(new URL(file_str + System.getProperty("user.dir") + "/src/main/java/itea/project/controllers/root.fxml"));
                loadArticleLayout(new URL(file_str + System.getProperty("user.dir") + "/src/main/java/itea/project/controllers/article.fxml"));
                loadSupplierLayout(new URL(file_str + System.getProperty("user.dir") + "/src/main/java/itea/project/controllers/supplier.fxml"));
            } else {
                loadRootLayout(getClass().getResource("/itea/project/controllers/root.fxml"));
                loadArticleLayout(getClass().getResource("/itea/project/controllers/article.fxml"));
                loadSupplierLayout(getClass().getResource("/itea/project/controllers/supplier.fxml"));
            }

            final int wVal = 10;
            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                root.setPrefWidth(primaryStage.getWidth() - wVal);
                articleLayout.setPrefWidth(primaryStage.getWidth() - wVal);
                supplierLayout.setPrefWidth(primaryStage.getWidth() - wVal);

            });
            final int hVal = 85;
            primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
                root.setPrefHeight(primaryStage.getHeight() - hVal);
                articleLayout.setPrefHeight(primaryStage.getHeight() - hVal);
                supplierLayout.setPrefHeight(primaryStage.getHeight() - hVal);

            });

            Platform.runLater(() -> {
                primaryStage.setMinWidth(Integer.valueOf(ini.getParam("ROOT","MinWidth")));
                primaryStage.setMinHeight(Integer.valueOf(ini.getParam("ROOT","MinHeight")));

                primaryStage.setWidth(Integer.valueOf(ini.getParam("ROOT","PrefWidth")));
                primaryStage.setHeight(Integer.valueOf(ini.getParam("ROOT","PrefHeight")));
                primaryStage.setHeight(primaryStage.getHeight()+1);
            });

            primaryStage.setTitle("InfoHUB v1.1");
            primaryStage.getIcons().add(new Image("/img/favicon.png"));
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(true);
            primaryStage.show();

            primaryStage.setOnCloseRequest(e -> {
                LOGGER.info("==================== PROGRAM END ====================");
                ini.setParam("ROOT", "PrefWidth", String.valueOf(primaryStage.getWidth()).replace(".0", ""));
                ini.setParam("ROOT", "PrefHeight", String.valueOf(primaryStage.getHeight()).replace(".0", ""));
                isAppInit = false;
            });
        } catch (Exception e) {
            LOGGER.error(getStackTrace(e));
            Platform.exit();
        }
    }

    public void setArticleLayout() {
        root.setCenter(articleLayout);
        articleController.setFocus();
    }

    public void setSupplierLayout() {
        root.setCenter(supplierLayout);
        supplierController.setFocus();
    }


    private void loadArticleLayout(URL path) throws IOException {
        WeakReference<FXMLLoader> loader = new WeakReference<>(new FXMLLoader());
        loader.get().setLocation(path);
        articleLayout = loader.get().load();
        articleController = loader.get().getController();
        articleController.setMainApp(this);
        loader.clear();
    }

    private void loadSupplierLayout(URL path) throws IOException {
        WeakReference<FXMLLoader> loader = new WeakReference<>(new FXMLLoader());
        loader.get().setLocation(path);
        supplierLayout = loader.get().load();
        supplierController = loader.get().getController();
        supplierController.setMainApp(this);
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
    public void start(Stage primaryStage) {
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
