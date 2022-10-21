package io.Adrestus.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public final class IPFinder {

    public static String getLocalIP() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
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
            return ip;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL not working, make sure you intern connection is working");
        } catch (IOException e) {
            throw new IllegalArgumentException("Make sure you intern connection is working");
        }
    }
}
