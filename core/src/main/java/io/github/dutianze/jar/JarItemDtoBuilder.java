package io.github.dutianze.jar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class JarItemDtoBuilder {

    private final String name;
    private final String jarPath;
    private Texture icon;

    public JarItemDtoBuilder(String name, String jarPath) {
        this.name = name;
        this.jarPath = jarPath;
    }

    public void setIcon(Texture icon) {
        this.icon = icon;
    }

    public JarItemDto build() {
        return new JarItemDto(name, jarPath,
                              icon != null ? icon : new Texture(Gdx.files.internal("libgdx.png")));
    }
}
