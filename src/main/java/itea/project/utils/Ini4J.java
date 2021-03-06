package itea.project.utils;

import javafx.application.Platform;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static itea.project.MainApp.LOGGER;
import static itea.project.utils.FxUtils.alertError;
import static itea.project.utils.Utils.extractJarResFolder;

//realizes the Singleton pattern
public class Ini4J {

    private Ini ini;
    private static Ini4J ini4J;
    private final String iniFileName = "Config.ini";

    private Ini4J() {
        try {
            extractJarResFolder(Collections.singletonList(iniFileName),".");
            ini = new Ini();
            ini.load(new File(iniFileName));
            checkIni();
        } catch (Exception e) {
            alertError(e);
            Platform.exit();
        }
    }

    public synchronized List<String> getSectionKeys(String section) {
        List<String> list = new ArrayList<>();
        if (ini != null) {
            if (ini.containsKey(section)) {
                list.addAll(ini.get(section).keySet());
            } else {
                alertError(new Exception("No such SECTION \"" + section + "\" in INI file"));
            }
        } else {
            alertError(new Exception("The INI instance was not initialized (= NULL) "));
            Platform.exit();
        }
        return list;
    }

    public synchronized List<String> getSectionValues(String section) {
        List<String> list = new ArrayList<>();
        if (ini != null) {
            if (ini.containsKey(section)) {
                list.addAll(ini.get(section).values());
            } else {
                alertError(new Exception("No such SECTION \"" + section + "\" in INI file"));
            }
        } else {
            alertError(new Exception("The INI instance was not initialized (= NULL) "));
            Platform.exit();
        }
        return list;
    }

    public synchronized static Ini4J getInstance() {
        if (ini4J == null) {
            ini4J = new Ini4J();
        }
        return ini4J;
    }

    public synchronized String getParam(String section, String param) {
        if (ini != null) {
            if (ini.containsKey(section) && ini.get(section).containsKey(param)) {
                return ini.get(section).get(param);
            } else {
                alertError(new Exception("No such SECTION \"" + section + "\" or KEY \"" + param + "\" in INI file"));
            }
        } else {
            alertError(new Exception("The INI instance was not initialized (= NULL) "));
            Platform.exit();
        }
        return null;
    }

    public boolean checkParam(String section, String param) {
        if (ini != null) {
            if (ini.containsKey(section) && ini.get(section).containsValue(param)) {
                return true;
            } else {
                return false;
            }
        } else {
            alertError(new Exception("The INI instance was not initialized (= NULL) "));
            Platform.exit();
        }
        return false;
    }

    public synchronized void setParam(String section, String param, String value) {
        if (ini != null) {
            if (ini.containsKey(section)) {
                try {
                    ini.get(section).put(param, value);
                    ini.store(new File(iniFileName));
                } catch (Exception e) {
                    alertError(e);
                }
            } else {
                alertError(new Exception("No such SECTION \"" + section + "\" or KEY \"" + param + "\" in INI file"));
            }
        } else {
            alertError(new Exception("The INI instance was not initialized (= NULL) "));
            Platform.exit();
        }
    }

    public void checkIni() throws IOException {
        if (ini != null) {
            SoftReference<Ini> resIni = new SoftReference<>(new Ini());
            resIni.get().load(getClass().getResource("/" + iniFileName));
            for (String sec : resIni.get().keySet()) {
                Profile.Section section = resIni.get().get(sec);
                if (!ini.containsKey(sec)) {
                    ini.add(sec);
                }
                for (String key : section.keySet()) {
                    if (!ini.get(sec).containsKey(key)) {
                        ini.get(sec).put(key, section.get(key));
                        LOGGER.info("Restored the INI param \"" + key + " = " + section.get(key) + "\" in \"" + sec + "\" section as it was missing in " + iniFileName);
                    }
                }
            }
            ini.store(new File(iniFileName));
        } else {
            alertError(new Exception("The INI instance was not initialized (= NULL) "));
            Platform.exit();
        }
    }
}
