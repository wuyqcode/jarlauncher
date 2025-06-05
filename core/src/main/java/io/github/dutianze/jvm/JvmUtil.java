package io.github.dutianze.jvm;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Generic Solution
 */
public class JvmUtil {

    private static void startJava(Path jar, String... appArgs) throws InterruptedException {
        List<String> commandline = new ArrayList<>();
        commandline.add(JavaRuntime.getDefault().getBinary().toString());
        commandline.add("-jar");
        commandline.add(jar.toAbsolutePath().toString());
        commandline.addAll(Arrays.asList(appArgs));
//        LOG.info("Starting process: " + commandline);

        NuProcessBuilder pb = new NuProcessBuilder(commandline);
        ProcessHandler handler = new ProcessHandler();
        pb.setProcessListener(handler);
        pb.setCwd(Paths.get(""));
        NuProcess process = pb.start();

        ByteBuffer buffer = ByteBuffer.wrap("Hello, World!".getBytes());
        process.writeStdin(buffer);

        process.waitFor(0, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        JvmUtil.startJava(Path.of("C:\\Users\\du.tianze\\Downloads\\tradingviewoader-1.0-SNAPSHOT.jar"));
    }

}
