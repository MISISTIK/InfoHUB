package itea.project.utils;

import itea.project.MainApp;
import javafx.application.Application;
import javafx.application.Platform;
import org.ini4j.Ini;

import java.io.IOException;

import static itea.project.utils.FxUtils.alertError;

//realizes the Singleton pattern
public class Ini4J {

    private Ini ini;
    private static Ini4J ini4J;

    private Ini4J() {
        try {
            ini = new Ini();
            ini.load(MainApp.class.getResource("/Config.ini"));
        } catch (IOException e) {
            alertError(e);
            Platform.exit();
        }
    }

    public synchronized static Ini4J getInstance() {
        if (ini4J == null) {
            ini4J = new Ini4J();
        }
        return ini4J;
    }

    public synchronized String getParam(String section, String param) {
        if (ini != null) {
            try {
                return ini.get(section).get(param);
            } catch (Exception e) {
                alertError(new Exception("The INI instance was not initialized (= NULL) "));
                Platform.exit();
            }
        } else {
            alertError(new Exception("The INI instance was not initialized (= NULL) "));
            Platform.exit();
        }
        return null;
    }
}
