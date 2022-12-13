package io.Adrestus.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public final class IPFinder {

    private static String local_address;
    private static String external_address;


    public static String getLocal_address() {
        if (local_address == null)
            getLocalIP();
        return local_address;
    }

    public static void setLocal_address(String local_address) {
        IPFinder.local_address = local_address;
    }

    public static String getExternal_address() {
        if (external_address == null)
            getExternalIP();
        return external_address;
    }

    public static void setExternal_address(String external_address) {
        IPFinder.external_address = external_address;
    }

    public static String getLocalIP() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            String local_ip = socket.getLocalAddress().getHostAddress();
            setLocal_address(local_ip);
            return local_ip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Make sure you intern connection is working");
    }

    public static String getExternalIP() {
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine(); //you get the IP as a String
            setExternal_address(ip);
            return ip;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL not working, make sure you intern connection is working");
        } catch (IOException e) {
            throw new IllegalArgumentException("Make sure you intern connection is working");
        }
    }
}
