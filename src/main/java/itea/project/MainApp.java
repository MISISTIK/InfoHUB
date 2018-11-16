package itea.project;

import itea.project.utils.Ini4J;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;

import java.net.URL;

public class MainApp extends Application
{
    private static final Logger log = LogManager.getLogger();
    private Ini4J ini;

    @Override
    public void init() {
        ini = Ini4J.getInstance();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("/view/root.fxml"));
//        Parent root = FXMLLoader.load(new URL("file://" + System.getProperty("user.dir") + "/src/main/java/itea/project/controllers/root.fxml"));
//        primaryStage.setTitle("InfoHUB");
//        primaryStage.getIcons().add(new Image("/img/favicon.png"));
//        primaryStage.setScene(new Scene(root));
//        primaryStage.setResizable(true);
//        primaryStage.show();

//        Class.forName(ini.getParam("CONNECTION","StoreDriver"));

        log.info("Info");
        Platform.exit();

    }

    public static void main(String[] args) {launch(args);}
}
