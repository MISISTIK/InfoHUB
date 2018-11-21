package itea.project;

import itea.project.controllers.ArticleController;
import itea.project.controllers.Controller;
import itea.project.controllers.RootController;
import itea.project.controllers.MultibaseController;
import itea.project.utils.Ini4J;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
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
    public static Logger LOGGER = LogManager.getLogger();
    public static volatile boolean isAppInit;
    private BorderPane root = null;
    private AnchorPane articleLayout = null;
    private AnchorPane multibaseLayout = null;
    private ArticleController articleController = null;
    private MultibaseController multibaseController = null;

    public Stage primaryStage;
    private Ini4J ini;


    //TODO Change dev to false
    private void initLayout(boolean isDev) {
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
                loadMultibaseLayout(new URL(file_str + System.getProperty("user.dir") + "/src/main/java/itea/project/controllers/multibase.fxml"));
            } else {
                loadRootLayout(getClass().getResource("/view/root.fxml"));
                loadArticleLayout(getClass().getResource("/view/article.fxml"));
                loadMultibaseLayout(getClass().getResource("/view/multibase.fxml"));
            }

            final int wVal = 10;
            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                root.setPrefWidth(primaryStage.getWidth() - wVal);
                articleLayout.setPrefWidth(primaryStage.getWidth() - wVal);
                multibaseLayout.setPrefWidth(primaryStage.getWidth() - wVal);

            });
            final int hVal = 85;
            primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
                root.setPrefHeight(primaryStage.getHeight() - hVal);
                articleLayout.setPrefHeight(primaryStage.getHeight() - hVal);
                multibaseLayout.setPrefHeight(primaryStage.getHeight() - hVal);

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
        primaryStage.getScene().setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.F1) {
                setArticleLayout();
            }
            if (code == KeyCode.F2) {
                setSupplierLayout();
            }
            if (event.isControlDown()) {
                if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
                    articleController.getTabPane().getSelectionModel().select(0);
                }
                if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
                    articleController.getTabPane().getSelectionModel().select(1);
                }
                if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
                    articleController.getTabPane().getSelectionModel().select(2);
                }
            }
            event.consume();
        });
    }

    public void setSupplierLayout() {
        root.setCenter(multibaseLayout);
        multibaseController.setFocus();
        primaryStage.getScene().setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.F1) {
                setArticleLayout();
            }
            if (code == KeyCode.F2) {
                setSupplierLayout();
            }
            if (event.isControlDown()) {
                if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
                    multibaseController.getTabPane().getSelectionModel().select(0);
                }
                if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
                    multibaseController.getTabPane().getSelectionModel().select(1);
                }
                if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
                    multibaseController.getTabPane().getSelectionModel().select(2);
                }
                if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4) {
                    multibaseController.getTabPane().getSelectionModel().select(3);
                }
            }
            event.consume();
        });
    }


    private void loadArticleLayout(URL path) throws IOException {
        WeakReference<FXMLLoader> loader = new WeakReference<>(new FXMLLoader());
        loader.get().setLocation(path);
        articleLayout = loader.get().load();
        articleController = loader.get().getController();
        articleController.setMainApp(this);
        loader.clear();
    }

    private void loadMultibaseLayout(URL path) throws IOException {
        WeakReference<FXMLLoader> loader = new WeakReference<>(new FXMLLoader());
        loader.get().setLocation(path);
        multibaseLayout = loader.get().load();
        multibaseController = loader.get().getController();
        multibaseController.setMainApp(this);
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
        boolean dev = false;
        initLayout(dev);
        setLayout(articleLayout,articleController);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
