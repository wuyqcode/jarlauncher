package io.github.dutianze;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class LoadingActor extends Image {

    private final Animation<TextureRegion> animation;
    private float stateTime;

    public LoadingActor(Skin skin) {
        Array<TextureRegion> regions = skin.getRegions("loading-bar");
        animation = new Animation<>(0.1f, regions);
        stateTime = 0;
        setDrawable(new TextureRegionDrawable(animation.getKeyFrame(0)));
    }

    @Override
    public void act(float delta) {
        stateTime += delta;
        stateTime %= animation.getAnimationDuration();
        ((TextureRegionDrawable) getDrawable()).setRegion(animation.getKeyFrame(stateTime, true));
        super.act(delta);
    }
}
