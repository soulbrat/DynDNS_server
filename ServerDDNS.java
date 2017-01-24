package com.my;

import com.my.exception.ZoneAuthenticationException;

import javax.security.sasl.AuthenticationException;
import java.io.*;
import java.net.Socket;
import java.util.ResourceBundle;

import static com.my.Manager.RESOURCE_PATH;

public class ServerDDNS implements Runnable {

    private static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "settings");

    public Socket socket;
    public InputStream inputSocket;
    public OutputStream outputSocket;

    public ServerDDNS(Socket socket) throws Throwable {
        this.socket = socket;
        this.inputSocket = socket.getInputStream();
        this.outputSocket = socket.getOutputStream();
    }

    public void run() {
        try {

            String line;
            long threadNumber = Thread.currentThread().getId();
            String remoteIP = socket.getRemoteSocketAddress().toString();
            boolean isOK = true;
            String answer;

            // GET AUTH data from Client
            line = readInput();

            String[] authData = line.split(" : ");
            String username = authData[0];
            String password = authData[1];
            Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + "Check user name: " + username + ", password: " + password);

            // authorization
            if (!Helper.checkUser(username, password)) {
                answer = "Authentication failed!";
                Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + answer);
                writeResponse(answer);

                socket.close();
                throw new AuthenticationException();
            }

            if (Helper.checkUser(username, password)) {

                answer = "Authentication successfully!";
                Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + answer);
                writeResponse(answer);

                // GET data from Client
                line = readInput();
                Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + line);

                String[] z = line.split(" : "); // example: 2.2.2.2 : example.com

                String ip = z[0];
                String zone = z[1];

                if (!Helper.checkZone(zone)) {
                    answer = "Zone " + zone + " is NOT correct!";
                    Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + answer);
                    writeResponse(answer);
                    isOK = false;
                }
                if (!Helper.checkIP(ip)) {
                    answer = "IP-address '" + ip + "' is NOT correct!";
                    Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + answer);
                    writeResponse(answer);
                    isOK = false;
                }

                if (isOK) {
                    answer = "All data is correct! " + ip + " : " + zone;
                    Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + answer);
                }

                // check user rights for zone
                if (!Helper.checkUserRights(username, password, zone)) {
                    answer = "Denied! You don't have rights for this zone! " + zone;
                    Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + answer);

                    writeResponse(answer);

                    throw new ZoneAuthenticationException();
                } else {
                    answer = "Rights is correct, we can start work with zone";
                    Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + answer);
                }

                if (isOK) {

                    String[] s = Helper.getZone(zone).split(" ");
                    // update zone with new IP
                    if (!s[3].equals(ip)) {

                        Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + "Zone is outdated and has to be updated!");

                        answer = Helper.updateZone(zone, ip);
                        writeResponse(answer);

                    } else {
                        answer = "Zone is updated! Please wait TTL time";
                        Helper.writeMessage(threadNumber + "(" + remoteIP + "): " + answer);
                        writeResponse(answer);
                    }
                }
            }
        } catch (Throwable t) {
                /*do nothing*/
            Helper.writeMessage(t.toString());
        }
        finally {
            try {
                socket.close();
            } catch (Throwable t) {
                Helper.writeMessage(t.toString());
            }
        }

        Helper.writeMessage("Client processing finished");
    }

    private void writeResponse(String response) throws Throwable {
        DataOutputStream out = new DataOutputStream(outputSocket);

        /* encode message */
        response = Helper.encodeString(response);
        /* encode message */

        out.writeUTF(response); //write answer
        out.flush();
    }

    private String readInput() throws Throwable {
        // Take input stream and now we can get data from client
        InputStream sin = socket.getInputStream();
        // Convert stream to other type
        DataInputStream in = new DataInputStream(sin);
        // Waiting message from client
        String line = in.readUTF();

        /* decode message */
        line = Helper.decodeString(line);
        /* decode message */

        return line;
    }


}

