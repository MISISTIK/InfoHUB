package itea.project.utils;

import itea.project.model.DataRow;
import itea.project.model.TableData;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static itea.project.utils.FxUtils.alertError;

public class SQLThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    List<DataRow> result;
    String[] headers;
    private String url;
    private String SQL;
    private Connection conn;
    private Thread thisThread;
    private ResultSet res;
    private TableData tableData;
    private Ini4J ini;

    public SQLThread(String SQL, String url, TableData td) {
        this.SQL = SQL;
        this.url = url;
        this.tableData = td;
        this.thisThread = new Thread(this);
        result = new ArrayList<>();
        ini = Ini4J.getInstance();
    }

    public SQLThread(String SQL, String url, TableData td, String threadName) {
        this(SQL, url, td);
        this.thisThread.setName(threadName);
    }

    @Override
    public void run() {
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection established for " + thisThread.getName() + " thread");
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            res = preparedStatement.executeQuery();
            if (res != null) {
                if (res.next()) {
                    ResultSetMetaData rsmd = res.getMetaData();
                    int colCount = rsmd.getColumnCount();
                    headers = new String[colCount];
                    for (int i = 0; i < colCount; i++) {
                        headers[i] = rsmd.getColumnName(i + 1);
                    }
                    //This imitates the real life DB delay
                    if (Boolean.parseBoolean(ini.getParam("ROOT","UseDelay"))) {
                        TimeUnit.SECONDS.sleep((int) (Math.random() * Integer.parseInt(ini.getParam("ROOT", "maxDelay"))) + Integer.parseInt(ini.getParam("ROOT", "minDelay")));
                    }
                    do {
                        Object[] d_array = new Object[colCount];
                        for (int i = 0; i < colCount; i++) {
                            d_array[i] = res.getObject(i + 1);
                        }
                        result.add(new DataRow(d_array));
                    } while (res.next());
                    System.out.println("[" + thisThread.getName() + "] " + "ResultSet -> DataRow DONE");
                    tableData.setHeaders(headers);
                    tableData.addList(result);
                } else {
                    LOGGER.warn("ResultSet of " + thisThread.getName() + " is empty");
                }
            } else {
                LOGGER.warn("ResultSet of " + thisThread.getName() + " = NULL");
            }
            System.out.println("Done " + thisThread.getName() + " thread");
        } catch (Exception e) {
            LOGGER.error("Exception " + thisThread.getName() + " ", e);
        } finally {
            this.CloseConnection();
        }
    }

    public List<DataRow> getResult() {
        return result;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void start() {
        thisThread.start();
    }

    public void join() {
        try {
            thisThread.join();
        } catch (InterruptedException e) {
            alertError(e);
        }
    }

    public void CloseConnection() {
        try {
            if (res != null) res.close();
            if (conn != null) conn.close();
            //System.out.println("Connection "+this.getName()+" closed");
        } catch (SQLException e) {
            alertError(new Exception("Connection " + this.thisThread.getName() + " already closed"));
        }
    }
}
