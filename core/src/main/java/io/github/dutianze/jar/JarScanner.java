package io.github.dutianze.jar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.github.dutianze.desktop.IconLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;

public class JarScanner {

    private final FileHandle directory;
    private final List<JarResourceLoader> loaders = List.of(new IconLoader());

    public JarScanner(FileHandle directory) {
        this.directory = directory;
    }

    public List<JarItemDto> scan() {
        List<JarItemDto> items = new ArrayList<>();

        if (!directory.isDirectory()) {
            Gdx.app.error("JarScanner", "Not a directory: " + directory.path());
            return items;
        }

        for (FileHandle file : directory.list()) {
            if (file.isDirectory()) {
                Optional<FileHandle> jarOptional = Arrays.stream(file.list())
                                                         .filter(f -> f.extension().equalsIgnoreCase("jar"))
                                                         .findFirst();
                if (jarOptional.isPresent()) {
                    JarItemDto item = extractJarInfo(jarOptional.get()
                    );
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }

    private JarItemDto extractJarInfo(FileHandle jarFileHandle) {
        String name = jarFileHandle.name();
        String jarPath = jarFileHandle.file().getAbsolutePath();
        JarItemDtoBuilder builder = new JarItemDtoBuilder(name, jarPath);

        try (JarFile jarFile = new JarFile(jarFileHandle.file())) {
            for (JarResourceLoader loader : loaders) {
                loader.load(jarFile, builder, name);
            }
        } catch (IOException e) {
            Gdx.app.error("JarScanner", "Failed to open jar: " + name, e);
        }

        return builder.build();
    }
}
