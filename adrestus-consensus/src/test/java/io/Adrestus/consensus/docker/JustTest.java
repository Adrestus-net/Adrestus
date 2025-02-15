package io.Adrestus.consensus.docker;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class JustTest {


    @Test
    public void test() throws SocketException {
        String arg0 = System.getProperty("test.arg0");
        String arg1 = System.getProperty("test.arg1");
        String arg2 = System.getProperty("test.arg2");
        System.out.println(getCurrentIPAddress() + "  " + arg0 + " " + arg1 + " " + arg2);
        if (isRunningInDocker()) {
            System.out.println("Running inside Docker container");
        } else {
            System.out.println("Not running inside Docker container");
        }
    }

    public static String getCurrentIPAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            // Skip loopback and inactive interfaces
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            // Iterate through the IP addresses assigned to the interface
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();

                // Skip IPv6 addresses if you only want IPv4
                if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(':') == -1) {
                    return inetAddress.getHostAddress();
                }
            }
        }

        return "No IP address found";
    }

    public static boolean isRunningInDocker() {
        try {
            return new File("/.dockerenv").exists();
        } catch (Exception e) {
            return false;
        }
    }
}
