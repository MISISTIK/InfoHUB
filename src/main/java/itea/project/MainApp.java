package itea.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application
{


    @Override
    public void start(Stage primaryStage) throws Exception {

//        Parent root = FXMLLoader.load(getClass().getResource("/view/root.fxml"));
        Parent root = FXMLLoader.load(new URL("file://" + System.getProperty("user.dir") + "/src/main/java/itea/project/controllers/root.fxml"));
        primaryStage.setTitle("InfoHUB");
        primaryStage.getIcons().add(new Image("/img/favicon.png"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.show();

    }

    public static void main(String[] args) {launch(args);}
}
