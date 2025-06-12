package io.github.dutianze.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class MenuItem extends Table {

    private final TextureRegionDrawable normalBg;
    private final TextureRegionDrawable hoverBg;

    public MenuItem(Skin skin, String name, String icon) {
        super(skin);
        Label label = new Label(name, skin, icon);
        Cell<Label> labelCell = this.add(label).left().expandX();
        if (LabelIcon.FILE.equals(icon) || LabelIcon.FOLDER.equals(icon)) {
            labelCell.padLeft(5);
        }

        if (LabelIcon.SHUTDOWN.equals(icon)) {
            this.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    Gdx.app.exit();
                    return true;
                }
            });
        }

        float width = 140f;
        float height = 32f;
        this.setTouchable(Touchable.enabled);

        normalBg = createFlatBackground((int) width, (int) height,
                                        new Color(237 / 255f, 238 / 255f, 224 / 255f, 0.1f));
        hoverBg = createHoverBackground((int) width, (int) height);

        this.setBackground(normalBg);

        this.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                              com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                setBackground(hoverBg);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                             com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                setBackground(normalBg);
            }
        });
    }

    private TextureRegionDrawable createFlatBackground(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private TextureRegionDrawable createHoverBackground(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        pixmap.setColor(Color.WHITE);
        pixmap.drawLine(0, height - 1, width - 1, height - 1);
        pixmap.drawLine(0, 0, 0, height - 1);

        pixmap.setColor(0.6f, 0.6f, 0.6f, 1f);
        pixmap.drawLine(0, 0, width - 1, 0);
        pixmap.drawLine(width - 1, 0, width - 1, height - 1);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
