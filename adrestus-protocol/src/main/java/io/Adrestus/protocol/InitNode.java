package io.Adrestus.protocol;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class InitNode {
    public static void main(String[] args) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(args[0]));

            System.out.println(prop.getProperty("user.mnemonic"));
            System.out.println(prop.getProperty("user.passphrace"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
