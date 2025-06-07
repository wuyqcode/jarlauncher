package io.github.dutianze.jar;

import com.badlogic.gdx.graphics.Texture;

public record JarItemDto(String name, String jarPath, Texture icon) {

    public String getIconName() {
        if (name == null) {
            return "";
        }
        int idx = name.lastIndexOf('-');
        return idx != -1 ? name.substring(0, idx) : name;
    }

}

