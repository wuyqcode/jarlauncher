package io.github.dutianze.desktop;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.dutianze.ShadowedLabel;
import io.github.dutianze.jar.JarItemDto;


public class DesktopIcon extends Table {

    public DesktopIcon(Skin skin, JarItemDto item, DesktopPane.IconClickListener clickListener) {
        super(skin);
        this.debug();
        this.setTouchable(Touchable.enabled);

        Image iconImage = new Image(item.icon());
        iconImage.setScaling(Scaling.contain);
        iconImage.setTouchable(Touchable.enabled);

        ShadowedLabel nameLabel = new ShadowedLabel(item.name(), skin, "white");

        this.add(iconImage).size(48).row();
        this.add(nameLabel).width(80).center();

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
    }

    @Override
    public float getPrefWidth() {
        return 80f;
    }

    @Override
    public float getPrefHeight() {
        return 90f;
    }

}
