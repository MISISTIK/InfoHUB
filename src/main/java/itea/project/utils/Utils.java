package itea.project.utils;

import itea.project.MainApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static itea.project.utils.FxUtils.alertError;

public class Utils {
    public static String[] getStoreList() {
        return Ini4J.getInstance().getParam("STORES_NUM","Stores_num").split(",");
    }

    public static void checkSQLFolder() {
        try {
            File sql_dir_res = new File(MainApp.class.getClassLoader().getResource("SQL").toURI());
            Path sql_dir_local = Paths.get("SQL");
            if (sql_dir_res.exists() && sql_dir_res.isDirectory()) {
                if (!Files.exists(sql_dir_local) || !Files.isDirectory(sql_dir_local)) {
                    if (!new File(sql_dir_local.toUri()).mkdir()) {
                        throw new Exception("Cannot create \"SQL\" dir here");
                    }
                }
                for (String sql_filename : sql_dir_res.list()) {
                    if (!Files.exists(Paths.get("SQL/" + sql_filename))) {
                        Files.copy(new File(sql_dir_res.getAbsolutePath() + "/" +sql_filename).toPath(),new File(sql_dir_local.toFile().getAbsolutePath() + "/" + sql_filename).toPath());
                    }
                }
            } else {
                System.out.println("No SQL directory in resources");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSQLFromFile(String sql_filename) {
        Path p = Paths.get("SQL/" + sql_filename);
        if (Files.exists(p)) {
            try {
                return String.join(" ",Files.readAllLines(p));
            } catch (Exception e) {
                alertError(e);
            }
        } else {
            alertError(new FileNotFoundException("No sql file with name \"" + sql_filename + "\" in SQL folder" ));
        }
        return null;
    }
}
