package io.github.dutianze.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import io.github.dutianze.jar.JarItemDto;
import io.github.dutianze.jvm.JavaRuntime;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LauncherWindow extends Window {

    private final NativeFileChooser fileChooser;
    private final TextField portField;
    private final Label statusLabel;
    private final float width = 400;
    private final float height = 350;
    private float originalX = (Gdx.graphics.getWidth() - width) / 2;
    private float originalY = (Gdx.graphics.getHeight() - height) / 2;

    private static final String LOG_PATH = "server.log";
    private Process serverProcess;
    private Program program;

    public LauncherWindow(Program program, Skin skin, NativeFileChooser fileChooser, JarItemDto jarItemDto) {
        super(jarItemDto.name(), skin);
        this.program = program;
        this.fileChooser = fileChooser;

        Table titleTable = getTitleTable();
        titleTable.clear();
        titleTable.center();

        Image icon = new Image(jarItemDto.icon());
        icon.setScaling(Scaling.fit);
        titleTable.add(icon).padRight(5);

        Label titleLabel = new Label(jarItemDto.name(), skin, "title");
        titleTable.add(titleLabel).padLeft(5).expandX().left();

        Table table = new Table(skin);
        table.setBackground("pane");
        table.defaults().pad(10.0f);

        Button closeButton = new Button(skin, "close");
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                program.close();
            }
        });
        Button minimizeButton = new TextButton("_", skin, "default");
        minimizeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleMinimize(false);
            }
        });

        titleTable.add(minimizeButton).expandX().right();
        titleTable.add(closeButton).padLeft(5.0f).padRight(5.0f);

        table.add(new Label("Port:", skin)).left();
        portField = new TextField("8080", skin);
        table.add(portField).growX().row();

        // Start/Stop buttons
        Table buttonTable = new Table();
        TextButton startButton = new TextButton("Start Server", skin);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startServer();
            }
        });
        TextButton stopButton = new TextButton("Stop Server", skin);
        stopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                stopServer();
            }
        });

        buttonTable.add(startButton).width(100).padRight(10);
        buttonTable.add(stopButton).width(100);
        table.add(buttonTable).colspan(2).left().growX();
        table.row();

        // Open Browser button
        TextButton openBrowserButton = new TextButton("Open Browser", skin);
        openBrowserButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openBrowser();
            }
        });
        table.add(openBrowserButton).colspan(2).center();
        table.row();

        // Open Log File button
        TextButton openLogButton = new TextButton("Open Log File", skin);
        openLogButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openLogFile();
            }
        });
        table.add(openLogButton).colspan(2).center();
        table.row();

        // Status label
        statusLabel = new Label("Status: Idle", skin);
        table.add(statusLabel).colspan(2).center();

        this.add(table).grow();
        this.setSize(400, 350);
    }


    public void toggleMinimize(boolean focus) {
        originalX = this.getX();
        originalY = this.getY();
        if (focus) {
            this.toFront();
            this.setWidth(width);
            this.setHeight(height);
            this.setPosition(originalX, originalY);
            this.setVisible(true);
            return;
        }

        this.setVisible(false);
    }


//    private void uploadJar() {
//        statusLabel.setText("Status: Opening file chooser...");
//        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
//        conf.directory = Gdx.files.absolute(System.getProperty("user.home"));
//        conf.title = "Choose JAR file";
//        conf.nameFilter = (dir, name) -> name.endsWith(".jar");
//
//        fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
//            @Override
//            public void onFileChosen(FileHandle file) {
//                try {
//                    String fileName = file.file().getName();
//                    Path sourcePath = Paths.get(file.path());
//                    Path targetPath = Paths.get(fileName);
//                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
//                    Gdx.app.postRunnable(() -> {
//                        statusLabel.setText("Status: JAR file uploaded");
//                        appendToLog(
//                            "JAR file uploaded: " + file.name() + " at " + getCurrentTime());
//                    });
//                } catch (IOException e) {
//                    Gdx.app.postRunnable(() -> {
//                        statusLabel.setText("Status: Upload failed - " + e.getMessage());
//                        appendToLog("Upload failed: " + e.getMessage());
//                    });
//                }
//            }
//
//            @Override
//            public void onCancellation() {
//                Gdx.app.postRunnable(() -> {
//                    statusLabel.setText("Status: Upload cancelled");
//                    appendToLog("Upload cancelled at " + getCurrentTime());
//                });
//            }
//
//            @Override
//            public void onError(Exception exception) {
//                Gdx.app.postRunnable(() -> {
//                    statusLabel.setText("Status: Upload failed - " + exception.getMessage());
//                    appendToLog("Upload failed: " + exception.getMessage());
//                });
//            }
//        });
//    }

    private void startServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            statusLabel.setText("Status: Server already running");
            return;
        }

        try {
            Path jarPath = Paths.get(program.getJarItemDto().jarPath());
            if (!Files.exists(jarPath)) {
                statusLabel.setText("Status: JAR file not found");
                return;
            }

            String port = portField.getText();
            if (!port.matches("\\d+")) {
                statusLabel.setText("Status: Invalid port number");
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(JavaRuntime.getDefault().getBinary().toString(),
                                                   "-jar", jarPath.toString(), "--server.port=" + port);
            pb.directory(jarPath.getParent().toFile());
            pb.redirectErrorStream(true);
            File logFile = new File(LOG_PATH);
            pb.redirectOutput(logFile);
            serverProcess = pb.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                serverProcess.destroy();
            }));

            AtomicBoolean statusFinalized = new AtomicBoolean(false);
            AtomicInteger dotCounter = new AtomicInteger(1);
            new Thread(() -> {
                try (RandomAccessFile raf = new RandomAccessFile(LOG_PATH, "r")) {
                    long filePointer = raf.length();

                    while (serverProcess.isAlive()) {
                        long fileLength = new File(LOG_PATH).length();
                        if (fileLength > filePointer) {
                            raf.seek(filePointer);
                            String line;
                            while ((line = raf.readLine()) != null) {
                                if (!statusFinalized.get()) {
                                    Gdx.app.postRunnable(() -> {
                                        if (!statusFinalized.get()) {
                                            int dots = dotCounter.getAndUpdate(
                                                n -> n >= 6 ? 1 : n + 1);
                                            String dotStr = ".".repeat(dots);
                                            Gdx.app.postRunnable(() -> {
                                                statusLabel.setText(
                                                    "Status: Starting server on port " + port
                                                    + dotStr);
                                            });
                                        }
                                    });
                                }

                                boolean isExceptionLine =
                                    line.startsWith("Exception in thread") ||
                                    line.startsWith("Caused by:") ||
                                    line.trim().startsWith("at ") ||
                                    line.matches(".*\\.(Exception|Error):.*");
                                if (line.contains("was already in use") || isExceptionLine) {
                                    if (statusFinalized.compareAndSet(false, true)) {
                                        Gdx.app.postRunnable(
                                            () -> statusLabel.setText("Status: error"));
                                    }
                                }

                                if (line.toLowerCase().contains("completed initialization")) {
                                    if (statusFinalized.compareAndSet(false, true)) {
                                        Gdx.app.postRunnable(
                                            () -> statusLabel.setText(
                                                "Status: Server started on port " + port));
                                    }
                                }
                            }
                            filePointer = raf.getFilePointer();
                        }
                        Thread.sleep(50);
                    }
                } catch (IOException | InterruptedException e) {
                    Gdx.app.postRunnable(
                        () -> statusLabel.setText("Status: Server error - " + e.getMessage()));
                    appendToLog("Server error: " + e.getMessage());
                }
            }).start();

            statusLabel.setText("Status: Starting server on port " + port + "...");
            appendToLog("Starting server on port " + port + " at " + getCurrentTime());
        } catch (IOException e) {
            statusLabel.setText("Status: Failed to start server - " + e.getMessage());
            appendToLog("Failed to start server: " + e.getMessage());
        }
    }

    private void stopServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
            try {
                serverProcess.waitFor();
                statusLabel.setText("Status: Server stopped");
                appendToLog("Server stopped at " + getCurrentTime());
            } catch (InterruptedException e) {
                statusLabel.setText("Status: Failed to stop server - " + e.getMessage());
                appendToLog("Failed to stop server: " + e.getMessage());
            }
            serverProcess = null;
        } else {
            statusLabel.setText("Status: No server running");
            appendToLog("No server running at " + getCurrentTime());
        }
    }

    private void openBrowser() {
        String port = portField.getText();
        if (!port.matches("\\d+")) {
            statusLabel.setText("Status: Invalid port number");
            appendToLog("Invalid port number: " + port);
            return;
        }

        String url = "http://localhost:" + port;
        boolean success = Gdx.net.openURI(url);
        if (success) {
            statusLabel.setText("Status: Browser opened");
            appendToLog("Opened browser to " + url + " at " + getCurrentTime());
        } else {
            statusLabel.setText("Status: Failed to open browser");
            appendToLog("Failed to open browser to " + url + " at " + getCurrentTime());
        }
    }

    private void openLogFile() {
        try {
            File logFile = new File(LOG_PATH);
            if (!logFile.exists()) {
                statusLabel.setText("Status: Log file not found");
                appendToLog("Log file not found at " + getCurrentTime());
                return;
            }

            // Convert file path to a file:// URL
            String fileUrl = logFile.toURI().toString();
            boolean success = Gdx.net.openURI(fileUrl);
            if (success) {
                statusLabel.setText("Status: Log file opened in browser");
                appendToLog("Log file opened in browser at " + getCurrentTime());
            } else {
                statusLabel.setText("Status: Failed to open log file in browser");
                appendToLog("Failed to open log file in browser at " + getCurrentTime());
            }
        } catch (Exception e) {
            statusLabel.setText("Status: Failed to open log file - " + e.getMessage());
            appendToLog("Failed to open log file: " + e.getMessage());
        }
    }

    private void appendToLog(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_PATH, true))) {
            writer.write("[" + getCurrentTime() + "] " + message);
            writer.newLine();
        } catch (IOException e) {
            Gdx.app.postRunnable(
                () -> statusLabel.setText("Status: Failed to write to log - " + e.getMessage()));
        }
    }


    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a").format(new Date());
    }
}
