package io.github.dutianze.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import io.github.dutianze.Main;
import io.github.dutianze.jar.JarItemDto;
import io.github.dutianze.taskbar.TrayButton;

import java.util.List;

/**
 * @author dutianze
 */
public class Program {

    private DesktopIcon desktopIcon;
    private TrayButton trayButton;
    private LauncherWindow launcherWindow;
    private final Main main;
    private Skin skin;
    private JarItemDto jarItemDto;
    private Stage stage;
    private NativeFileChooser fileChooser;
    private boolean focus;

    public Program(Main main, Skin skin, Stage stage, NativeFileChooser fileChooser, JarItemDto jarItemDto) {
        this.main = main;
        this.skin = skin;
        this.jarItemDto = jarItemDto;
        this.stage = stage;
        this.fileChooser = fileChooser;
        this.trayButton = new TrayButton(jarItemDto.getIconName(), jarItemDto.icon(), skin);
        this.launcherWindow = new LauncherWindow(this, skin, fileChooser, jarItemDto);
        this.desktopIcon = new DesktopIcon(skin, jarItemDto, item -> {
            Program.this.open();
            focus(this, true, false);
        });
        launcherWindow.setPosition(
            (Gdx.graphics.getWidth() - launcherWindow.getWidth()) / 2,
            (Gdx.graphics.getHeight() - launcherWindow.getHeight()) / 2
        );

        this.trayButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                focus(Program.this, !Program.this.isFocus(), true);
            }
        });

        this.launcherWindow.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Program.this.focus(Program.this, true, false);
                return false;
            }
        });

        stage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                boolean anyHit = false;
                for (Program program : main.getOpenedProgramList()) {
                    LauncherWindow launcherWindow = program.getLauncherWindow();
                    Vector2 windowLocalCoordinates = launcherWindow.stageToLocalCoordinates(new Vector2(x, y));
                    Actor hit = launcherWindow.hit(windowLocalCoordinates.x, windowLocalCoordinates.y, true);
                    if (hit != null && hit.isDescendantOf(launcherWindow)) {
                        anyHit = true;
                    }
                }
                if (!anyHit) {
                    main.getOpenedProgramList().forEach(e -> {
                        e.focus(e, false, false);
                    });
                }
                return false;
            }
        });

    }

    public void focus(Program program, boolean focus, boolean minimize) {
        program.focus = focus;
        program.trayButton.setChecked(program.focus);
        if (minimize) {
            program.getLauncherWindow().toggleMinimize(focus);
        }

        getOthers().forEach(e -> {
            e.focus = false;
            e.trayButton.setChecked(false);
        });
        main.getRoot().invalidateHierarchy();
    }


    public List<Program> getOthers() {
        return main.getOpenedProgramList().stream().filter(e -> !e.equals(Program.this)).toList();
    }


    public void addToTaskBar(Table taskbarButtonArea) {
        taskbarButtonArea.add(this.trayButton).height(28).padLeft(5).padTop(2).padBottom(2);
    }

    public TrayButton getTrayButton() {
        return trayButton;
    }

    public DesktopIcon getDesktopIcon() {
        return desktopIcon;
    }

    public LauncherWindow getLauncherWindow() {
        return launcherWindow;
    }

    public void open() {
        List<Program> openedProgramList = this.main.getOpenedProgramList();
        if (openedProgramList.contains(this)) {
            return;
        }

        this.main.getOpenedProgramList().add(this);
        Table taskbarContent = this.main.getTaskbarContent();
        taskbarContent.clear();
        taskbarContent.left();
        this.main.getOpenedProgramList()
                 .forEach(program -> program.addToTaskBar(taskbarContent));
        stage.addActor(launcherWindow);
    }

    public void close() {
        List<Program> openedProgramList = this.main.getOpenedProgramList();
        if (!openedProgramList.contains(this)) {
            return;
        }

        this.main.getOpenedProgramList().remove(this);
        this.trayButton.remove();
        this.launcherWindow.remove();
    }

    public JarItemDto getJarItemDto() {
        return jarItemDto;
    }

    public boolean isFocus() {
        return focus;
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
    }
}
