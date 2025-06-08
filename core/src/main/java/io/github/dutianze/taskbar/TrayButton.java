package io.github.dutianze.taskbar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

public class TrayButton extends TextButton {

    public TrayButton(String text, Texture icon, Skin skin) {
        super("", createTrayXpStyle(skin));
        Table content = new Table();
        content.pad(2, 8, 2, 8);
        content.bottom().center().left().center();

        Image image = new Image(new TextureRegionDrawable(new TextureRegion(icon)));
        image.setSize(16, 16);
        image.setScaling(Scaling.contain);
        image.setAlign(Align.center);

        content.add(image).size(16, 16);
        content.add(new Label(text, skin)).padLeft(4);
        this.add(content).grow();
    }

    private static TextButtonStyle createTrayXpStyle(Skin skin) {
        TextButtonStyle style = new TextButtonStyle();

        style.font = skin.getFont("font");
        style.fontColor = Color.BLACK;

        style.up = createXp3dButtonDrawable(
            new Color(0.9f, 0.95f, 1.0f, .6f),
            new Color(0.75f, 0.85f, 1.0f, .6f),
            new Color(0f, 0f, 0f, .5f)
        );

        style.checked = createXp3dButtonDrawable(
                new Color(0.9f, 0.95f, 1.0f, 1f),
                new Color(0.75f, 0.85f, 1.0f, 1f),
                Color.WHITE
            );
        return style;
    }

    private static Drawable createXp3dButtonDrawable(Color topColor, Color bottomColor, Color border) {
        int width = 64;
        int height = 24;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            float t = (float) y / (height - 1);
            float r = topColor.r * (1 - t) + bottomColor.r * t;
            float g = topColor.g * (1 - t) + bottomColor.g * t;
            float b = topColor.b * (1 - t) + bottomColor.b * t;
            float a = topColor.a * (1 - t) + bottomColor.a * t;
            pixmap.setColor(r, g, b, a);
            pixmap.drawLine(0, y, width - 1, y);
        }

        pixmap.setColor(border);
        pixmap.drawLine(0, 0, 0, height - 1);
        pixmap.drawLine(0, 0, width - 1, 0);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

}
