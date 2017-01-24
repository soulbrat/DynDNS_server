package com.my;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.net.util.IPAddressUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static com.my.Manager.RESOURCE_PATH;

public class Helper {

    private static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "settings");
    public static final String currentDir = System.getProperty("user.dir");
    public static final String logFile = currentDir + res.getString("FILE.LOG");

    /* crypt */
    private static String encodeKey = res.getString("ENCODE.KEY");
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static BASE64Encoder enc = new BASE64Encoder();
    private static BASE64Decoder dec = new BASE64Decoder();
    /* crypt */

    public static void writeMessage(String line)
    {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String message = dateFormat.format(date) + ": " + line;
        System.out.println(message);
        saveLog(message);
    }

    public static boolean checkIP(String ipAddressString) {
        final Pattern PATTERN_IP = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        boolean isIP = false;
        if (PATTERN_IP.matcher(ipAddressString).matches()) {
            isIP = IPAddressUtil.isIPv4LiteralAddress(ipAddressString);
        }
        return isIP;
    }

    public synchronized static boolean checkZone(String zone) {

        // check connection to DB
        try {
            JDBC_connector.isDbConnected();
        } catch (Exception e) {
            /* do nothing*/
        }

        // check if zone is exist
        boolean result = false;
        ArrayList<String> list = JDBC_connector.selectDB("select name from records where name='" + zone + "' and type='A';");
        for (String s : list)
        {
            if (s.contains(zone)) result = true;
        }
        return result;
    }

    public synchronized static String getZone(String zone) {

        // check connection to DB
        try {
            JDBC_connector.isDbConnected();
        } catch (Exception e) {
            /* do nothing*/
        }
        String result;

        ArrayList<String> list = JDBC_connector.selectDB("select name,type,content,ttl from records where name='" + zone + "' and type='A';");
        String zoneName = list.get(0);
        String zoneType = list.get(1);
        String zoneIP = list.get(2);
        String zoneTTL = list.get(3);

        result = zoneName + " " + zoneType + " " + zoneTTL + " " + zoneIP;

        return result;
    }

    public synchronized static String updateZone(String zone, String ip)
    {
        // check connection to DB
        try {
            JDBC_connector.isDbConnected();
        } catch (Exception e) {
            /* do nothing*/
        }
        // update records set content
        String result = "";
        boolean isQuery = JDBC_connector.updateDB("update records set content='" + ip +"' where  name='" + zone + "' and type='A';");
        if (isQuery){
            result = "Domain was updated successfully!";
        } else {
            result = "Error while update!";
        }
        return result;
    }

    public synchronized static boolean checkUser(String username, String password){
        boolean result = false;

        try {
            // check connection to DB
            JDBC_connector.isDbConnected();
            //
            ArrayList<String> list = JDBC_connector.selectDB("select username,password from users where username='" + username + "' and password='" + password + "';");
            if ((list.get(0).equals(username) && list.get(1).equals(password))) {
                result = true;
            }
            return result;
        }catch (Exception e){
            //Helper.writeMessage(e.toString());
            return false;
        }
    }

    public synchronized static boolean checkUserRights(String username, String password, String zone){
        boolean result = false;

        try {
            ArrayList<String> list = JDBC_connector.selectDB("SELECT domains.name FROM domains LEFT JOIN zones ON zones.domain_id=domains.id RIGHT JOIN users ON zones.owner=users.id WHERE username='" + username + "' and password='" + password + "';");
            for(String s: list){
                if (s.equals(zone)){
                    result = true;
                    break;
                }
            }
            return result;

        }catch (Exception e){
        //Helper.writeMessage(e.toString());
        return false;
        }
    }

    public static void saveLog(String line)
    {
        File file = new File(logFile);
        try {

            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fop = new FileOutputStream(file,true);
            if(line!=null) {
                fop.write(line.getBytes());
                fop.write("\n".getBytes());
            }
            fop.flush();
            fop.close();

        }catch (Exception e){
            System.out.println(e);
            saveLog(e.toString());
        }
    }


    /* crypt */
    public static String base64encode(String text) {
        try {
            return enc.encode(text.getBytes(DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }//base64encode

    public static String base64decode(String text) {
        try {
            return new String(dec.decodeBuffer(text), DEFAULT_ENCODING);
        } catch (IOException e) {
            return null;
        }
    }//base64decode

    public static String xorMessage(String message, String key) {
        try {
            if (message == null || key == null) return null;

            char[] keys = key.toCharArray();
            char[] mesg = message.toCharArray();

            int ml = mesg.length;
            int kl = keys.length;
            char[] newmsg = new char[ml];

            for (int i = 0; i < ml; i++) {
                newmsg[i] = (char)(mesg[i] ^ keys[i % kl]);
            }//for i

            return new String(newmsg);
        } catch (Exception e) {
            return null;
        }
    }//xorMessage

    public static String encodeString(String line) {
        //System.out.println("Line for encode: " + line);
        String line1 = Helper.xorMessage(line, encodeKey);
        String encodedLine = Helper.base64encode(line1);
        //System.out.println("Encoded line: " + encodedLine);
        return encodedLine;
    }
    public static String decodeString(String line){
        //System.out.println("Line for decode: " + line);
        String line1 = Helper.base64decode(line);
        String decodedLine = Helper.xorMessage(line1, encodeKey);
        //System.out.println("Decoded line: " + decodedLine);
        return decodedLine;
    }
    /* crypt */
}
