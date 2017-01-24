package com.my;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;

public class Manager {

    public static final String RESOURCE_PATH = "com.my.resources.";
    private static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "settings");

    public static void main(String[] args) throws Throwable {

        // global catch
        try {

            // re-create log file //
            File file = new File(Helper.logFile);
            if(file.exists()) {
                file.delete();
            }
            ////////////////////////

            // create server port
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(res.getString("SOCKET.SERVER.PORT")));
            Helper.writeMessage("SocketServer created successfully");

            // create new mysql connection
            JDBC_connector jdbc_connector = new JDBC_connector();

            // start work with clients
            while (true) {

                // check mysql connection and create new one if down
                if (!JDBC_connector.checkConnection()) {
                    new JDBC_connector();
                }

                Helper.writeMessage("Waiting for new clients...");
                Socket socket = serverSocket.accept(); // waiting for client
                Helper.writeMessage("Client accepted");

                // check mysql connection and create new one if down
                if (!JDBC_connector.checkConnection()) {
                    new JDBC_connector();
                }

                // return to client result code of operation
                try {
                    // create Thread
                    new Thread(new ServerDDNS(socket)).start();

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

        // global catch
        }catch (Exception e)
        {
            Helper.writeMessage("Global catch error");
            Helper.writeMessage(e.toString());
        }
    }
}
