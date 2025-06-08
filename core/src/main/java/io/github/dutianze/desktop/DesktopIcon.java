package io.github.dutianze.desktop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.dutianze.jar.JarItemDto;


public class DesktopIcon extends Table {

    public DesktopIcon(Skin skin, JarItemDto item, DesktopPane.IconClickListener clickListener) {
        super(skin);
        this.setTouchable(Touchable.enabled);
        Image iconImage = new Image(item.icon());
        iconImage.setScaling(Scaling.contain);
        iconImage.setTouchable(Touchable.enabled);
        IconTitle nameLabel = new IconTitle(item.getIconName(), skin, "default");
        this.add(iconImage).size(this.getPrefWidth() * 3 / 5).row();
        this.add(nameLabel).width(this.getPrefWidth()).center();
        this.addListener(new ClickListener() {
            long lastClickTime = 0;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                long currentTime = TimeUtils.millis();
                if (currentTime - lastClickTime < 500) {
                    clickListener.onDoubleClick(item);
                }
                lastClickTime = currentTime;
                return true;
            }
        });


        Drawable hoverBackground = createHoverBackground();
        Drawable transparentBackground = createTransparentBackground();
        this.addListener(new ClickListener() {
            long lastClickTime = 0;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                long currentTime = TimeUtils.millis();
                if (currentTime - lastClickTime < 500) {
                    clickListener.onDoubleClick(item);
                }
                lastClickTime = currentTime;
                return true;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                setBackground(hoverBackground);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                setBackground(transparentBackground);
            }
        });

    }

    @Override
    public float getPrefWidth() {
        return 90f;
    }

    @Override
    public float getPrefHeight() {
        return 90f;
    }

    private Drawable createTransparentBackground() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);  // 完全透明
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private Drawable createHoverBackground() {
        int width = (int) this.getPrefWidth();
        int height = (int) this.getPrefHeight();

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        pixmap.setColor(new Color(189f / 255, 221f / 255, 255f / 255, 0.4f));
        pixmap.fillRectangle(0, 0, width, height);


        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }


}
