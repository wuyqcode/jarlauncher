package io.github.dutianze.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
        });
        launcherWindow.setPosition(
            (Gdx.graphics.getWidth() - launcherWindow.getWidth()) / 2,
            (Gdx.graphics.getHeight() - launcherWindow.getHeight()) / 2
        );
        this.trayButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                launcherWindow.toggleMinimize();
            }
        });
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

}
