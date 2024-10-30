package io.Adrestus.config;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class Directory {
    private static Logger LOG = LoggerFactory.getLogger(Directory.class);
    private static final String CREDENTIALS_APP_NAME = "AdrestusApplication";
    private static final String CREDENTIALS_AUTHOR = "Adrestus";
    private static final String CREDENTIALS_VERSION = "v1.0";

    private static volatile Directory instance;
    private static volatile AppDirs appDirs;

    private Directory() {
        // Protect against instantiation via reflection
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    /**
     * The instance doesn't get created until the method is called for the first time.
     */
    public static synchronized Directory getInstance() {
        if (instance == null) {
            synchronized (Directory.class) {
                if (instance == null) {
                    instance = new Directory();
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.startsWith("mac os x")) {
                        // LOG.debug("os.name {} is resolved to Mac OS X", os);
                        appDirs = new MacOSXAppDirs();
                    } else if (os.startsWith("windows")) {
                        // LOG.debug("os.name {} is resolved to Windows", os);
                        WindowsFolderResolver folderResolver = new ShellFolderResolver();
                        appDirs = new WindowsAppDirs(folderResolver);
                    } else {
                        // Assume other *nix.
                        //LOG.debug("os.name {} is resolved to *nix", os);
                        appDirs = new UnixAppDirs();
                    }
                }
            }
        }
        return instance;
    }

    public static synchronized String getConfigPath() {
        Directory.getInstance();
        String path = appDirs.getUserDataDir(CREDENTIALS_APP_NAME, CREDENTIALS_VERSION, CREDENTIALS_AUTHOR);
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static synchronized String getConfigPathPlusPathName(String path) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("windows")) {
            return getConfigPath() + "\\" + path;
        } else if (os.startsWith("mac os x")) {
            return getConfigPath() + "/" + path;
        } else {
            return getConfigPath() + "/" + path;
        }
    }

    public static synchronized String CreateFolderPath(String name) {
        Path path = Paths.get(getConfigPathPlusPathName(name));
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }

            Path created = Files.createDirectories(path);
            return created.toString();
        } catch (IOException e) {
            e.printStackTrace();

        }
        throw new RuntimeException("Failed to create folder");
    }

    public static void deleteKafkaLogFiles(File folder) {
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                deleteKafkaLogFiles(file);
            }
        }
        folder.delete();
    }

}
