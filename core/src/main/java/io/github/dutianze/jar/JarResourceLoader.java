package io.github.dutianze.jar;

import java.util.jar.JarFile;

/**
 * @author Generic Solution
 */
public interface JarResourceLoader {

    void load(JarFile jarFile, JarItemDtoBuilder builder, String jarName);
}

