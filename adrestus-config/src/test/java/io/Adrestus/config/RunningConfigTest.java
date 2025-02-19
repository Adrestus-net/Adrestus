package io.Adrestus.config;

import org.junit.jupiter.api.Test;

public class RunningConfigTest {



    @Test
    public void test(){
        System.out.println("RunningConfigTest Docker: "+RunningConfig.isRunningInDocker());
        System.out.println("RunningConfigTest Appveyor: "+RunningConfig.isRunningInAppveyor());
        System.out.println("RunningConfigTest Maven: "+RunningConfig.isRunningInMaven());
    }
}
