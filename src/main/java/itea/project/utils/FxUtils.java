package itea.project.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FxUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        return s;
    }

    public static synchronized void alertError(Exception e) {
        LOGGER.error(getStackTrace(e));
        if (Platform.isAccessibilityActive()) {
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
