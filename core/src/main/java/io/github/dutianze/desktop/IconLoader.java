package io.github.dutianze.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import io.github.dutianze.jar.JarItemDtoBuilder;
import io.github.dutianze.jar.JarResourceLoader;

import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class IconLoader implements JarResourceLoader {

    @Override
    public void load(JarFile jarFile, JarItemDtoBuilder builder, String jarName) {
        try {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return;
            }

            String iconPath = manifest.getMainAttributes().getValue("App-Icon");
            if (iconPath == null) {
                return;
            }

            ZipEntry entry = jarFile.getEntry(iconPath);
            if (entry == null) {
                Gdx.app.error("IconLoader", "Icon not found: " + iconPath);
                return;
            }

            try (InputStream is = jarFile.getInputStream(entry)) {
                byte[] bytes = is.readAllBytes();
                Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
                builder.setIcon(new Texture(pixmap));
                pixmap.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("IconLoader", "Failed to load icon from jar: " + jarName, e);
        }
    }
}
