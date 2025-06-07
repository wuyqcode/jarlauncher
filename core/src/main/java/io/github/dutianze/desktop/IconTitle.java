package io.github.dutianze.desktop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;


public class IconTitle extends Label {


    public IconTitle(String text, Skin skin, String styleName, float fontScale, int alignment) {
        super(text, skin, styleName);

        this.setFontScale(fontScale);
        this.setEllipsis(true);
        this.setWrap(false);
        this.setColor(Color.BLACK);
        this.setFontScale(fontScale);
        this.setAlignment(alignment);
    }

    public IconTitle(String text, Skin skin, String styleName) {
        this(text, skin, styleName, 1.2f, Align.center);
    }
}
