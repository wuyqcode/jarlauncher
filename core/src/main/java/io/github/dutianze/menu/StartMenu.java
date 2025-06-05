package io.github.dutianze.menu;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.List;

public class StartMenu extends Table {

    private static TextureRegionDrawable backgroundDrawable;

    public StartMenu(Stage stage, Button startButton, Skin skin) {
        super(skin);
        this.bottom().left();
        List<MenuItem> items = List.of(
            new MenuItem(skin, "My Computer", LabelIcon.FOLDER),
            new MenuItem(skin, "Documents", LabelIcon.FILE),
            new MenuItem(skin, "Pictures", LabelIcon.FILE),
            new MenuItem(skin, "Settings", LabelIcon.FILE),
            new MenuItem(skin, "shutdown", LabelIcon.SHUTDOWN)
        );

        for (MenuItem item : items) {
            this.row();
            Cell<MenuItem> menuItemCell = this.add(item);
            menuItemCell.padLeft(2);
            menuItemCell.padTop(2);
            menuItemCell.growX();
        }

        float itemHeight = 32f + 2f;
        float width = 140f + 2;
        float height = items.size() * itemHeight;

        this.setWidth(width);
        this.setHeight(height);
        this.setVisible(false);
        this.setY(32.0f);

        if (backgroundDrawable == null) {
            backgroundDrawable = createStyleBackground((int) width, (int) height);
        }

        this.setBackground(backgroundDrawable);

        stage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (StartMenu.this.isVisible()) {
                    Vector2 localCoordinates = StartMenu.this.stageToLocalCoordinates(
                        new Vector2(x, y));
                    Actor hit = StartMenu.this.hit(localCoordinates.x, localCoordinates.y, true);
                    if (hit == null || !hit.isDescendantOf(StartMenu.this)) {
                        StartMenu.this.setVisible(false);
                        startButton.setChecked(false);
                    }
                }
                return false;
            }
        });
        this.setDebug(true);
        this.setClip(true);
    }

    private TextureRegionDrawable createStyleBackground(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(237 / 255f, 238 / 255f, 224 / 255f, 1f);
        pixmap.fill();

        pixmap.setColor(0.7f, 0.7f, 0.7f, 1f);
        pixmap.drawRectangle(0, 0, width - 1, height - 1);

        pixmap.setColor(0.5f, 0.5f, 0.5f, 1f);
        pixmap.drawLine(1, 0, width - 2, 0);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
