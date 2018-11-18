package itea.project.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class SQLThread implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger();
    private ResultSet result;
    private String url;
    private String name;
    private String password;
    private String SQL;
    private Connection conn;
    public Thread thisThread;

    public SQLThread(String SQL,String url, String name, String password) {
        this.SQL = SQL;
        this.url = url;
        this.name = name;
        this.password = password;
        this.thisThread = new Thread(this);
    }

    public SQLThread (String SQL,String url, String name, String password, String threadName) {
        this(SQL,url,name,password);
        this.thisThread.setName(threadName);
    }

    @Override
    public void run() {
        try {
            conn = DriverManager.getConnection(url, name, password);
            System.out.println("Connection established for " + thisThread.getName() + " thread");
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            result = preparedStatement.executeQuery();
            System.out.println("Done " + thisThread.getName() + " thread");
        } catch (Exception e) {
            LOGGER.error("Exception " + thisThread.getName()+ " " ,e);
            this.CloseConnection();
        }
    }

    public void CloseConnection() {
        try {
            if (conn != null) conn.close();
            if (result != null) result.close();
            //System.out.println("Connection "+this.getName()+" closed");
        } catch (SQLException e) {
            LOGGER.error("Connection "+this.thisThread.getName()+" already closed");
        }
    }

    public ResultSet getResult(){
        return result;
    }
}
