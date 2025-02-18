//package io.Adrestus.consensus.docker;
//
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.model.Image;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.testcontainers.DockerClientFactory;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.containers.Network;
//import org.testcontainers.images.builder.ImageFromDockerfile;
//import org.testcontainers.shaded.org.awaitility.Awaitility;
//import org.testcontainers.utility.DockerImageName;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.Duration;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.regex.Pattern;
//
//public class ConsensusDockerTest2 {
//
//    private static final Logger LOG = LoggerFactory.getLogger(ConsensusDockerTest2.class);
//
//    private static final String DOCKERFILE_NAME = "DockerfileVMBuild";
//    private static final String IMAGE_NAME = "dockerfile-vm-build";
//    private static final String IMAGE_TAG = "latest";
//    private static final String FULL_IMAGE_NAME = IMAGE_NAME + ":" + IMAGE_TAG;
//    private static final String VRF_TEST_NAME = "ConsensusVRFTest2";
//
//    // IP configuration
//    private static final String NETWORK_ALIAS = "consensus-network";
//    private static final List<String> IP_ADDRESSES = Arrays.asList(
//            "192.168.100.2",
//            "192.168.100.3",
//            "192.168.100.4"
//    );
//
//    // Exception pattern matching
//    private static final Pattern EXCEPTION_PATTERN = Pattern.compile(
//            "(Exception|kafka.common.errors|at\\s+\\S+\\:\\d+|Caused by\\:)"
//    );
//
//    private static Network network;
//    private static DockerImageName customImage;
//
//    private static String getCurrentMethodName() {
//        return Thread.currentThread().getStackTrace()[2].getMethodName();
//    }
//
//    public static boolean isRunningInAppveyor() {
//        return System.getenv("APPVEYOR_BUILD_ID") != null &&
//                System.getenv("APPVEYOR_BUILD_NUMBER") != null;
//    }
//
//    public static boolean containsException(String logContent) {
//        return EXCEPTION_PATTERN.matcher(logContent).find();
//    }
//
//
//    private static boolean imageExists(String imageName) {
//        DockerClient dockerClient = DockerClientFactory.instance().client();
//        List<Image> images = dockerClient.listImagesCmd().exec();
//
//        return images.stream()
//                .flatMap(image -> Arrays.stream(Optional.ofNullable(image.getRepoTags()).orElse(new String[0])))
//                .anyMatch(tag -> tag.equals(imageName));
//    }
//
//    private static DockerImageName buildOrGetImage() {
//        if (imageExists(FULL_IMAGE_NAME)) {
//            LOG.info("Image {} already exists, skipping build", FULL_IMAGE_NAME);
//            return DockerImageName.parse(FULL_IMAGE_NAME);
//        }
//
//        LOG.info("Image {} does not exist, building new image", FULL_IMAGE_NAME);
//        Path projectDirectory = Paths.get(System.getProperty("user.dir"));
//        Path dockerfilePath = projectDirectory.getParent().resolve(DOCKERFILE_NAME);
//
//        if (!dockerfilePath.toFile().exists()) {
//            throw new IllegalArgumentException("Dockerfile does not exist at " + dockerfilePath);
//        }
//
//        new ImageFromDockerfile(FULL_IMAGE_NAME, false)
//                .withDockerfile(dockerfilePath)
//                //withFileFromPath(dockerfilePath.toAbsolutePath().toString(), dockerfilePath)
//                .get(); // This triggers the build
//
//        return DockerImageName.parse(FULL_IMAGE_NAME);
//    }
//
//    @BeforeAll
//    public static void setup() {
////        if (1 == 1) return; // Keeping your conditional check
//
//        // Create custom network
//        network = Network.newNetwork();
//
//        // Build or get existing image
//        customImage = buildOrGetImage();
//    }
//
//    @Test
//    public void VRFTest() throws InterruptedException {
//        GenericContainer<?> container = new GenericContainer<>(customImage)
//                .withNetwork(network)
//                .withNetworkAliases("container-" + "containerIndex")
//                .withCreateContainerCmdModifier(cmd -> {
//                    cmd.withIpv4Address(IP_ADDRESSES.get(0));
//                })
//                .withWorkingDirectory("/adrestus-consensus/")
//                .withFileSystemBind(new java.io.File("./src").getAbsolutePath(), "/adrestus-consensus/src")
//                .withCommand("sh", "-c",
//                        String.format(
//                                "mvn clean install -DskipTests && mvn test -Dtest=%s -Dtest.arg0=%s -Dtest.arg1=%s -Dtest.arg2=%s",
//                                VRF_TEST_NAME,
//                                IP_ADDRESSES.get(0),
//                                IP_ADDRESSES.get(1),
//                                IP_ADDRESSES.get(2)
//                        )
//                )
//                .withStartupTimeout(Duration.ofMinutes(5));
//
//        container.start();
//        Awaitility
//                .await()
//                .atMost(Duration.ofMinutes(5))
//                .untilAsserted(() -> {
//                    String logs =
//                            container.execInContainer(
//                                    "sh",
//                                    "-c",
//                                    "cat /proc/1/fd/1"
//                            ).getStdout();
//
//                    long count = logs.lines().filter(line -> line.contains("Hello World")).count();
//                    if (count == 5) {
//                        System.out.println("The string 'Hello World' appears 5 times.");
//                    } else {
//                        System.out.println("The string 'Hello World' does not appear 5 times.");
//                    }
//                });
//        container.stop();
//    }
//}
