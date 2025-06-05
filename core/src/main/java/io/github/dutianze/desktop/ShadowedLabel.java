package io.github.dutianze;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;


public class ShadowedLabel extends Label {

    private final float outlineOffset;

    public ShadowedLabel(String text, Skin skin, String styleName, float fontScale,
                         float outlineOffset, int alignment) {
        super(text, skin, styleName);
        this.outlineOffset = outlineOffset;

        this.setFontScale(fontScale);
        this.setEllipsis(true);
        this.setWrap(false);
        this.setFontScale(fontScale);
        this.setAlignment(alignment);
    }

    public ShadowedLabel(String text, Skin skin, String styleName) {
        this(text, skin, styleName, 1f, 0.5f, Align.left);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX();
        float y = getY();
        float r = getColor().r, g = getColor().g, b = getColor().b, a = getColor().a;

        // 黑色边框
        float[][] offsets = {
            {-outlineOffset, 0}, {outlineOffset, 0},
            {0, -outlineOffset}, {0, outlineOffset},
            {-outlineOffset, -outlineOffset}, {-outlineOffset, outlineOffset},
            {outlineOffset, -outlineOffset}, {outlineOffset, outlineOffset}
        };

        setColor(0f, 0f, 0f, a);
        for (float[] offset : offsets) {
            setPosition(x + offset[0], y + offset[1]);
            super.draw(batch, parentAlpha);
        }

        // 恢复位置和颜色，绘制主文字
        setPosition(x, y);
        setColor(r, g, b, a);
        super.draw(batch, parentAlpha);
    }
}
