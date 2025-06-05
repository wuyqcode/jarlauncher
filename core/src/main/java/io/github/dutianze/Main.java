package io.github.dutianze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import io.github.dutianze.desktop.DesktopPane;
import io.github.dutianze.jar.JarItemDto;
import io.github.dutianze.jar.JarScanner;
import io.github.dutianze.jvm.JavaRuntime;
import io.github.dutianze.menu.StartMenu;
import io.github.dutianze.taskbar.TrayButton;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends ApplicationAdapter {

    private Stage stage;
    private Skin skin;
    private Process serverProcess;
    private TextField portField;
    private Label statusLabel;
    private Label timeLabel;
    private Label jarPathLabel;
    private static String JAR_PATH = "";
    private static final String LOG_PATH = "server.log";
    private Window window;
    private float originalWidth, originalHeight, originalX, originalY;
    private boolean isMinimized = false;
    private final NativeFileChooser fileChooser;

    private List<JarItemDto> jarItems;

    // 构造函数，接受 NativeFileChooser
    public Main(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    @Override
    public void create() {
        FileHandle jarDir = Gdx.files.internal("jar");
        JarScanner scanner = new JarScanner(jarDir);
        jarItems = scanner.scan();

        skin = new Skin(Gdx.files.internal("expee-ui.json"));
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        DesktopPane desktop = new DesktopPane(skin, jarItems, selectedItem -> {
            jarPathLabel.setText(selectedItem.jarPath());
            statusLabel.setText("Status: Selected " + selectedItem.name());
            window.setVisible(true);
            window.toFront();
        }, 5);

        Table taskbar = new Table(skin);
        taskbar.setBackground("taskbar");
        taskbar.left().padLeft(5);

        // Start Menu
        taskbar.left().bottom().pad(0);
        final Button startButton = new Button(skin, "start");
        final StartMenu startMenu = new StartMenu(stage, startButton, skin);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                startMenu.setVisible(startButton.isChecked());
                startMenu.toFront();
            }
        });
        stage.addActor(startMenu);

        taskbar.add(startButton);
        Table taskbarButtonArea = new Table(skin);
        taskbarButtonArea.background("taskbar");
        taskbar.add(taskbarButtonArea).growX();

        taskbarButtonArea.left();
        TrayButton taskbarButton = new TrayButton("jar launcher", "libgdx.png", skin);
        taskbarButtonArea.add(taskbarButton).height(28).padLeft(5).padTop(2).padBottom(2).center();
        taskbarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleMinimize();
            }
        });

        Table table = new Table(skin);
        table.setBackground("tray");
        timeLabel = new Label(getCurrentTime(), skin, "white");
        table.add(timeLabel).padLeft(5.0f);
        taskbar.add(table);

        root.top();
        root.add(desktop).expand().fill().row();
        root.add(taskbar).fillX().height(32).bottom();

        // JAR Launcher Window
        window = new Window("JAR Launcher", skin);
        Button closeButton = new Button(skin, "close");
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        Button minimizeButton = new TextButton("_", skin, "default");
        Table titleTable = window.getTitleTable();
        titleTable.add(minimizeButton).expandX().right();
        titleTable.add(closeButton).padLeft(5.0f).padRight(5.0f);
        table = new Table(skin);
        table.setBackground("pane");
        table.defaults().pad(10.0f);

        // Store original window properties
        originalWidth = 400;
        originalHeight = 350;
        originalX = (Gdx.graphics.getWidth() - originalWidth) / 2;
        originalY = (Gdx.graphics.getHeight() - originalHeight) / 2;

        minimizeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleMinimize();
            }
        });

        // File upload button
        TextButton uploadButton = new TextButton("Upload JAR", skin);
        uploadButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                uploadJar();
            }
        });
        table.add(new Label("JAR File:", skin)).left();
        table.add(uploadButton).growX();
        table.row();

        // JAR file path display
        table.add(new Label("JAR Path:", skin)).left();
        jarPathLabel = new Label(getJarPath(), skin);
        table.add(jarPathLabel).growX();
        table.row();

        // Port input
        table.add(new Label("Port:", skin)).left();
        portField = new TextField("8080", skin);
        table.add(portField).growX();
        table.row();

        // Start/Stop buttons
        Table buttonTable = new Table();
        TextButton startButtonServer = new TextButton("Start Server", skin);
        startButtonServer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startServer();
            }
        });
        buttonTable.add(startButtonServer).width(100).padRight(10);

        TextButton stopButton = new TextButton("Stop Server", skin);
        stopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                stopServer();
            }
        });
        buttonTable.add(stopButton).width(100);
        table.add(buttonTable).colspan(2).center();
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

        window.add(table).grow();
        window.setWidth(400);
        window.setHeight(350);
        window.setPosition(
            (Gdx.graphics.getWidth() - window.getWidth()) / 2,
            (Gdx.graphics.getHeight() - window.getHeight()) / 2
        );
        stage.addActor(window);
    }

    private void uploadJar() {
        statusLabel.setText("Status: Opening file chooser...");
        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
        conf.directory = Gdx.files.absolute(System.getProperty("user.home"));
        conf.title = "Choose JAR file";
        conf.nameFilter = (dir, name) -> name.endsWith(".jar");

        fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {
                try {
                    String fileName = file.file().getName();
                    Path sourcePath = Paths.get(file.path());
                    Path targetPath = Paths.get(fileName);
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    JAR_PATH = fileName;
                    Gdx.app.postRunnable(() -> {
                        statusLabel.setText("Status: JAR file uploaded");
                        jarPathLabel.setText(fileName);
                        appendToLog(
                            "JAR file uploaded: " + file.name() + " at " + getCurrentTime());
                    });
                } catch (IOException e) {
                    Gdx.app.postRunnable(() -> {
                        statusLabel.setText("Status: Upload failed - " + e.getMessage());
                        appendToLog("Upload failed: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onCancellation() {
                Gdx.app.postRunnable(() -> {
                    statusLabel.setText("Status: Upload cancelled");
                    appendToLog("Upload cancelled at " + getCurrentTime());
                });
            }

            @Override
            public void onError(Exception exception) {
                Gdx.app.postRunnable(() -> {
                    statusLabel.setText("Status: Upload failed - " + exception.getMessage());
                    appendToLog("Upload failed: " + exception.getMessage());
                });
            }
        });
    }

    private void toggleMinimize() {
        if (!isMinimized) {
            window.setVisible(false);
            isMinimized = true;
            statusLabel.setText("Status: Window minimized");
        } else {
            window.setVisible(true);
            window.setWidth(originalWidth);
            window.setHeight(originalHeight);
            window.setPosition(originalX, originalY);
            isMinimized = false;
            statusLabel.setText("Status: Window restored");
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        return sdf.format(new Date());
    }

    private String getJarPath() {
        Path path = Paths.get(JAR_PATH).toAbsolutePath();
        return Files.exists(path) ? path.getFileName().toString() : "No JAR uploaded";
    }

    private void startServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            statusLabel.setText("Status: Server already running");
            return;
        }

        try {
            Path jarPath = Paths.get(JAR_PATH);
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
                                                   "-jar", JAR_PATH, "--server.port=" + port);
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

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        timeLabel.setText(getCurrentTime());
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (serverProcess != null) {
            serverProcess.destroy();
        }
        stage.dispose();
        skin.dispose();
    }
}
