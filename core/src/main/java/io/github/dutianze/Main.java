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
import io.github.dutianze.desktop.DesktopPane;
import io.github.dutianze.desktop.Program;
import io.github.dutianze.jar.JarItemDto;
import io.github.dutianze.jar.JarScanner;
import io.github.dutianze.menu.StartMenu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main extends ApplicationAdapter {

    private static final String LOG_PATH = "server.log";
    private Stage stage;
    private Skin skin;
    private Label timeLabel;
    private final NativeFileChooser fileChooser;
    private List<Program> programList = new ArrayList<>();
    private List<Program> openedProgramList = new ArrayList<>();
    private Table taskbarContent;
    private Table root;
    private FileHandle jarPath;
    private JarScanner scanner;
    private DesktopPane desktop;

    // 构造函数，接受 NativeFileChooser
    public Main(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("expee-ui.json"));
        this.jarPath = Gdx.files.local("jar");
        this.scanner = new JarScanner(jarPath);
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        this.root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);
        root.top();

        // desktop
        this.desktop = new DesktopPane(skin);
        scanJar();
        root.add(desktop).expand().fill().row();

        // taskbar
        Table taskbar = new Table(skin);
        taskbar.setBackground("taskbar");
        taskbar.left().padLeft(5);
        // taskbar left
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
        // taskbar content
        this.taskbarContent = new Table(skin);
        taskbarContent.background("taskbar");
        taskbar.add(taskbarContent).growX();
        // taskbar right
        Table table = new Table(skin);
        table.setBackground("tray");
        timeLabel = new Label(getCurrentTime(), skin, "white");
        table.add(timeLabel).padLeft(5.0f);
        taskbar.add(table);
        root.add(taskbar).fillX().height(32).bottom();
    }


    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        return sdf.format(new Date());
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
        stage.dispose();
        skin.dispose();
    }

    public List<Program> getProgramList() {
        return programList;
    }

    public Table getTaskbarContent() {
        return taskbarContent;
    }

    public List<Program> getOpenedProgramList() {
        return openedProgramList;
    }

    public void setOpenedProgramList(List<Program> openedProgramList) {
        this.openedProgramList = openedProgramList;
    }

    public Table getRoot() {
        return root;
    }

    public void setRoot(Table root) {
        this.root = root;
    }

    public void scanJar() {
        List<JarItemDto> jarItems = scanner.scan();
        this.programList = jarItems
            .stream().map(e -> new Program(this, skin, stage, fileChooser, e)).toList();
        this.desktop.setup(this.programList, 5);
        this.desktop.invalidateHierarchy();
    }

    public void uploadJar(Path uploadJar) {
        FileHandle source = Gdx.files.absolute(uploadJar.toAbsolutePath().toString());

        String folder = getIconName(uploadJar.toFile().getName());
        Path targetFile = Paths.get(folder).resolve(uploadJar.getFileName().toString());
        FileHandle target = jarPath.child(targetFile.toString());
        source.copyTo(target);
        scanJar();
        Gdx.app.postRunnable(() -> appendToLog(
            "JAR file uploaded: " + uploadJar.getFileName() + " at " + getCurrentTime()));
    }

    public String getIconName(String jarName) {
        if (jarName == null) {
            return "";
        }
        int idx = jarName.indexOf('-');
        return idx != -1 ? jarName.substring(0, idx) : jarName;
    }


    private void appendToLog(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_PATH, true))) {
            writer.write("[" + getCurrentTime() + "] " + message);
            writer.newLine();
        } catch (IOException e) {
            Gdx.app.error("Main", "appendToLog", e);
        }
    }

}
