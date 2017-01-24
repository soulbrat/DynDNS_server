package com.my;

import com.my.exception.MySQLNonTransientConnectionException;

import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.my.Manager.RESOURCE_PATH;

public class JDBC_connector {

    private static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "settings");

    private static Connection connection = null;

    public JDBC_connector() throws Exception {
        while (true) {
            try {
                //Load of driver
                Class.forName("com.mysql.jdbc.Driver");
                Helper.writeMessage("Driver MYSQL connected");
                //Create connections
                this.connection = DriverManager.getConnection(url, name, password);
                Helper.writeMessage("MYSQL connection created");
                break;
            }catch (Exception e)
            {
                Helper.writeMessage("MySQL server is not available!");
                Helper.writeMessage(e.toString());
                Thread.sleep(5000);
            }
        }
    }

    public static boolean checkConnection() throws SQLException {
        boolean result = false;
        if (!connection.isClosed()) result = true;
        return result;
    }

    public static void isDbConnected() throws Exception {
        final String request = "show tables;";
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery(request);
        } catch (SQLException | NullPointerException e) {
            Helper.writeMessage("DB Connection lost! Try to re-connect.");
            new JDBC_connector();
        }
    }

    //String url = "jdbc:postgresql://127.0.0.1:5432/test";
    private static String url = res.getString("MYSQL_STRING");
    //DB username
    private static String name = res.getString("MYSQL_USERNAME");
    //DB password
    private static String password = res.getString("MYSQL_PASSWORD");

    public static ArrayList<String> selectDB(String request) {

        ArrayList<String> list = new ArrayList<String>();

        try {

            if (connection.isClosed()) {
                throw new MySQLNonTransientConnectionException();
            }

            //1.Statement: for easy case without params
            Statement statement = connection.createStatement();
            // executeQuery only for SELECT! // for INSERT use executeUpdate!
            //String query = "show tables;";
            ResultSet rs = statement.executeQuery(request);

            //
            int columns = rs.getMetaData().getColumnCount();

            //
            while (rs.next()) {
                for (int i = 1; i <= columns; i++) {
                    //Helper.writeMessage(rs.getString(i) + "\t");
                    // add to array
                    list.add(rs.getString(i));
                }
            }
            if (rs != null)
                rs.close();
            if (statement != null)
                statement.close();
            return list;

        } catch (Exception ex) {
            Helper.writeMessage(ex.toString());
            return null;
        }
    }

    public static boolean updateDB(String query) {

        boolean result = false;

        try {
            Statement statement = connection.createStatement();
            // input record and get result
            int check = statement.executeUpdate(query);
            //check
            if (check == 1) result = true;

            if (statement != null)
                statement.close();

        } catch (Exception ex) {
            Helper.writeMessage(ex.toString());
        }

        return result;
    }
}
