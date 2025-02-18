package io.Adrestus.consensus.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import io.Adrestus.config.RunningConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.Adrestus.config.RunningConfig.isRunningInAppveyor;

public class ConsensusDockerTest {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusDockerTest.class);

    private static final String dockerFileName = "DockerfileVMBuild";
    private static final String imageAdrestusVM = "dockerfile-vm-build:latest";
    private static final String VRFTestName = "JustTest";
    private static final String networkName = "network";
//    private static final String imageConsensusName = "dockerfile-consensus-tests";

    // Regex pattern to match common exception patterns
    private static final String exceptionPattern = "(Exception|kafka.common.errors|at\\s+\\S+\\:\\d+|Caused by\\:)";
    private static final Pattern pattern = Pattern.compile(exceptionPattern);

    private static DockerClient dockerClient;
    private static CreateNetworkResponse network;

    // Define the IPv4 address
    private static final String ipv4Address1 = "192.168.100.2";
    private static final String ipv4Address2 = "192.168.100.3";
    private static final String ipv4Address3 = "192.168.100.4";
    private static ArrayList<String> ipv4Addresses;

    private static String getCurrentMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }


    public static boolean containsException(String logContent) {
        Matcher matcher = pattern.matcher(logContent);
        return matcher.find();
    }

    @BeforeAll
    public static void setup() throws InterruptedException {
        if (isRunningInAppveyor()) {
            return;
        }
//        if (1 == 1) {
//            return;
//        }
        ipv4Addresses = new ArrayList<>(Arrays.asList(ipv4Address1, ipv4Address2, ipv4Address3));
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(httpClient)
                .build();


        boolean networkExists = dockerClient.listNetworksCmd().exec().stream()
                .map(Network::getName)
                .anyMatch(name -> name.equals(networkName));

        if (networkExists) {
            dockerClient.removeNetworkCmd(networkName).exec();
        }
        CreateNetworkResponse network = dockerClient.createNetworkCmd()
                .withName(networkName)
                .withDriver("bridge")
                .withIpam(new Network.Ipam().withConfig(new Network.Ipam.Config().withSubnet("192.168.100.0/24")))
                .exec();


//        // Create and start the container
//        CreateContainerResponse container = dockerClient.createContainerCmd(imageAdrestusVM)
//                .withCmd("sh", "-c", "while true; do echo 'Hello, Docker!'; sleep 1; done")
//                .withHostConfig(HostConfig.newHostConfig()
//                        .withNetworkMode(networkName))
//                .withIpv4Address(ipv4Address)
//                .exec();
//
//        dockerClient.startContainerCmd(container.getId()).exec();
//
//        // Verify the IP address assignment
//        ContainerNetwork assignedNetwork = dockerClient.inspectContainerCmd(container.getId())
//                .exec()
//                .getNetworkSettings()
//                .getNetworks()
//                .get(networkName);
//
//        System.out.println("Assigned IPv4 Address: " + assignedNetwork.getIpAddress());
//
//
////         1. Create network
//        Network.Ipam.Config ipamConfig = new Network.Ipam.Config()
//                .withSubnet("192.168.100.0/24")
//                //.withIpRange("192.168.1.71/31") // Adjusted IP range to include two addresses
//                .withGateway("192.168.100.1");
//
//        Network.Ipam ipam = new Network.Ipam()
//                .withConfig(ipamConfig);
//
//        boolean networkExists1 = dockerClient.listNetworksCmd().exec().stream()
//                .map(Network::getName)
//                .anyMatch(name -> name.equals(networkName));
//
//        if (networkExists1) {
//            dockerClient.removeNetworkCmd(networkName).exec();
//        }
//        network = dockerClient.createNetworkCmd()
//                .withName("network")
//                .withDriver("macvlan")
//                .withOptions(new HashMap<String, String>() {{
//                    put("macvlan_mode", "bridge");
//                    put("parent", System.getProperty("os.name").toLowerCase().contains("win") ? "eth0" : "enp3s0");
//                }})
//                .withIpam(ipam)
//                .exec();

        // 2. Build image
        BuildImageResultCallback buildCallback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                System.out.println("Build output: " + item.getStream());
                super.onNext(item);
            }
        };

        boolean imageAdrestusVMExists = dockerClient.listImagesCmd().exec().stream()
                .flatMap(image -> Arrays.stream(image.getRepoTags()))
                .anyMatch(tag -> tag.equals(imageAdrestusVM));

        if (!imageAdrestusVMExists) {
            LOG.info("AdrestusVMExists not exist: {}", getCurrentMethodName());
            Path projectDirectory = Paths.get(System.getProperty("user.dir"));
            Path dockerfilePath = projectDirectory.getParent().resolve(dockerFileName);
            if (!dockerfilePath.toFile().exists()) {
                throw new IllegalArgumentException("Dockerfile does not exist at " + dockerfilePath.toString());
            }
            dockerClient.buildImageCmd(dockerfilePath.toFile())
                    .withTag(imageAdrestusVM)
                    .exec(buildCallback)
                    .awaitCompletion();
        } else {
            LOG.info("AdrestusVMExists exists: {}", getCurrentMethodName());
        }
    }


    @Test
    public void VRFTest() throws IOException {
//        if (1 == 1)
//            return;
        if (RunningConfig.isRunningInAppveyor()) {
            return;
        }

        ArrayList<CreateContainerResponse> containerResponses = new ArrayList<>();
        ArrayList<LogContainerCmd> logContainerCmds = new ArrayList<>();
        AtomicInteger containerCount = new AtomicInteger(ipv4Addresses.size());
        for (int i = 0; i < ipv4Addresses.size(); i++) {
            CreateContainerResponse container = null;
            try {
                String[] buildCommand = {
//                        "sh", "-c", "mvn clean install -DskipTests && mvn test -Dtest=" + VRFTestName +
                        "sh", "-c", "mvn clean install -DskipTests && mvn test -Dtest=" + VRFTestName +
                        " -Dtest.arg0=" + ipv4Addresses.get(0) +
                        " -Dtest.arg1=" + ipv4Addresses.get(1) +
                        " -Dtest.arg2=" + ipv4Addresses.get(2)
                };
                container = dockerClient
                        .createContainerCmd(imageAdrestusVM)
                        .withNetworkDisabled(false)
                        .withWorkingDir("/adrestus-consensus/")
                        .withName("Container" + String.valueOf(i))
                        .withCmd(buildCommand)
                        .withHostConfig(HostConfig
                                .newHostConfig()
                                .withNetworkMode(networkName)
                                .withBinds(new Bind(new File("./src").getAbsolutePath(), new Volume("/adrestus-consensus/src"))))
                        .withIpv4Address(ipv4Addresses.get(i))
                        .exec();

                containerResponses.add(container);
//                // 5. Connect container to the network
//                dockerClient.connectToNetworkCmd()
//                        .withNetworkId(network.getId())
//                        .withContainerId(container.getId())
//                        .exec();

//
//            System.out.println("Container connected to network: " + network.getId());
                String containerId = container.getId();
                // 6. Start container
                dockerClient.startContainerCmd(container.getId()).exec();
                System.out.println("Container started with ID: " + container.getId());

//                InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
//                ContainerNetwork network = inspectResponse.getNetworkSettings().getNetworks().get(networkName);
//
//                System.out.println("Assigned IPv4 Address: " + network.getIpAddress());

                // 7. Print log output
                LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(container.getId())
                        .withStdOut(true)
                        .withStdErr(true)
                        .withFollowStream(true);
                logContainerCmds.add(logContainerCmd);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        logContainerCmds.forEach(val -> {
            Thread.ofVirtual().start(() -> {
                try {
                    val.exec(new ResultCallback.Adapter<>() {
                        boolean first = false;

                        @Override
                        public void onNext(Frame item) {
                            String logLine = new String(item.getPayload());
//                            System.out.println(logLine);
                            if (logLine.contains(VRFTestName)) {
                                first = true;
                            }
                            if (first) {
                                System.out.println(val.getContainerId() + " " + logLine);
                                boolean containsException = containsException(logLine);
                                if (containsException) {
                                    throw new IllegalArgumentException("Exception found in" + "with log: " + logLine);
                                }
                            }
                        }
                    }).awaitCompletion();
                    containerCount.getAndDecrement();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } finally {
                }
            });
        });

        Awaitility
                .await()
                .atMost(Duration.ofMinutes(4))
                .untilAsserted(() -> {
                    if (containerCount.get() != 0) {
                        throw new AssertionError("Condition not met");
                    }
                });
        containerResponses.forEach(container -> {
            // Remove the container
            if (dockerClient.inspectContainerCmd(container.getId()).exec() != null) {
                dockerClient.removeContainerCmd(container.getId()).withForce(true).withRemoveVolumes(true).exec();
            }
        });
        dockerClient.close();
    }
}
