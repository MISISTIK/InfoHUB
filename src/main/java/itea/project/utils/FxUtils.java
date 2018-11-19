package itea.project.utils;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static itea.project.MainApp.LOGGER;
import static itea.project.MainApp.isAppInit;

public class FxUtils {

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        return s;
    }

    public static synchronized void alertError(Exception e) {
        LOGGER.error(getStackTrace(e));
        if (isAppInit) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error alert");
            alert.setHeaderText(e.getMessage());

            VBox dialogPaneContent = new VBox();

            Label label = new Label("Stack Trace:");

            TextArea textArea = new TextArea();
            textArea.setText(getStackTrace(e));

            dialogPaneContent.getChildren().addAll(label, textArea);
            alert.getDialogPane().setContent(dialogPaneContent);

            alert.showAndWait();
        } else
            LOGGER.error("The FX Applications was not initialized");
    }

}
