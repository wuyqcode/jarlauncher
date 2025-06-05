package io.github.dutianze.taskbar;

import com.badlogic.gdx.Gdx;
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

    public TrayButton(String text, String iconPath, Skin skin) {
        super("", createTrayXpStyle(skin));
        Table content = new Table();
        content.pad(2, 8, 2, 8);
        content.bottom().center().left().center();

        Texture texture = new Texture(Gdx.files.internal(iconPath));
        Drawable icon = new TextureRegionDrawable(new TextureRegion(texture));
        Image image = new Image(icon);
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
            // topColor：浅蓝
            new Color(0.82f, 0.88f, 0.97f, 1f),
            // bottomColor：深蓝
            new Color(0.64f, 0.76f, 0.94f, 1f),
            // 上左边框：高光
            Color.WHITE,
            // 下右边框：阴影
            new Color(0.4f, 0.5f, 0.7f, 1f)
        );

        style.over = createXp3dButtonDrawable(
            new Color(0.9f, 0.95f, 1.0f, 1f),
            new Color(0.7f, 0.85f, 1.0f, 1f),
            Color.WHITE,
            new Color(0.5f, 0.6f, 0.8f, 1f)
        );

        // 按下 → 高光和阴影反转，模拟凹陷
        style.down = createXp3dButtonDrawable(
            new Color(0.5f, 0.65f, 0.85f, 1f),
            new Color(0.65f, 0.8f, 0.95f, 1f),
            // 上左变暗
            new Color(0.3f, 0.4f, 0.6f, 1f),
            // 下右变亮
            Color.WHITE
        );

        return style;
    }

    private static Drawable createXp3dButtonDrawable(Color topColor, Color bottomColor,
                                                     Color borderLight, Color borderDark) {
        int width = 64;
        int height = 24;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // 背景渐变（手动垂直线性插值）
        for (int y = 0; y < height; y++) {
            float t = (float) y / (height - 1);
            float r = topColor.r * (1 - t) + bottomColor.r * t;
            float g = topColor.g * (1 - t) + bottomColor.g * t;
            float b = topColor.b * (1 - t) + bottomColor.b * t;
            float a = topColor.a * (1 - t) + bottomColor.a * t;
            pixmap.setColor(r, g, b, a);
            pixmap.drawLine(0, y, width - 1, y);
        }

        // 上左边：浅色
        pixmap.setColor(borderLight);
        // top
        pixmap.drawLine(0, height - 1, width - 1, height - 1);
        // left
        pixmap.drawLine(0, 0, 0, height - 1);

        // 下右边：深色
        pixmap.setColor(borderDark);
        // bottom
        pixmap.drawLine(0, 0, width - 1, 0);
        // right
        pixmap.drawLine(width - 1, 0, width - 1, height - 1);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

}
