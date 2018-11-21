package itea.project.utils;

import itea.project.model.DataRow;
import itea.project.model.TableData;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static itea.project.utils.FxUtils.alertError;

public class SQLThread implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    List<DataRow> result;
    String[] headers;
    private String url;
    private String name;
    private String password;
    private String SQL;
    private Connection conn;
    private Thread thisThread;
    private ResultSet res;
    private TableData tableData;
    private Ini4J ini;
    private Semaphore semaphore;


    public SQLThread(String SQL, String url, TableData td, Semaphore semaphore) {
        this.SQL = SQL;
        this.url = url;
        this.name = null;
        this.password = null;
        this.tableData = td;
        this.thisThread = new Thread(this);
        this.semaphore = semaphore;
        result = new ArrayList<>();
        ini = Ini4J.getInstance();
        this.thisThread.setDaemon(true);
    }

    public SQLThread(String SQL, String url, String name, String password, TableData td, Semaphore semaphore) {
        this(SQL, url, td, semaphore);
        this.name = name;
        this.password = password;
    }

    public SQLThread(String SQL, String url, String name, String password, TableData td, Semaphore semaphore, String threadName) {
        this(SQL, url, td, semaphore);
        this.name = name;
        this.password = password;
        this.thisThread.setName(threadName);
    }

    public SQLThread(String SQL, String url, TableData td, Semaphore semaphore, String threadName) {
        this(SQL, url, td, semaphore);
        this.thisThread.setName(threadName);
    }

    @Override
    public void run() {
        try {
            if (name != null && password != null) {
                conn = DriverManager.getConnection(url,name,password);
            } else {
                conn = DriverManager.getConnection(url);
            }
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
                    if (Boolean.parseBoolean(ini.getParam("ROOT", "UseDelay"))) {
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
                    semaphore.acquire();
                    Platform.runLater(() -> {
                        if (!Arrays.equals(tableData.getHeaders(), headers)) {
                            tableData.setHeaders(headers);
                        }
                        tableData.addList(result);
                    });
                    semaphore.release();
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
