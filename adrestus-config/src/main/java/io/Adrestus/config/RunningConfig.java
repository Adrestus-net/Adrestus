package io.Adrestus.config;

import java.io.File;

public final class RunningConfig {

    public static boolean isRunningInDocker() {
        return new File("/.dockerenv").exists();
    }

    public static boolean isRunningInAppveyor() {
        String buildId = System.getenv("APPVEYOR_BUILD_ID");
        String buildNumber = System.getenv("APPVEYOR_BUILD_NUMBER");
        return buildId != null && buildNumber != null;
    }

    public static boolean isRunningInMaven() {
        return System.out.getClass().getName().contains("maven");
    }
}
