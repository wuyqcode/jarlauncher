/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2024 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.dutianze.jvm;


import jakarta.annotation.Nullable;

/**
 * @author Glavo
 */
public final class JavaInfo {

    public static int parseVersion(String version) {
        int startIndex = version.startsWith("1.") ? 2 : 0;
        int endIndex = startIndex;

        while (endIndex < version.length()) {
            char ch = version.charAt(endIndex);
            if (ch >= '0' && ch <= '9') {
                endIndex++;
            } else {
                break;
            }
        }

        try {
            return endIndex > startIndex ? Integer.parseInt(version.substring(startIndex, endIndex)) : -1;
        } catch (Throwable e) {
            // The version number is too long
            return -1;
        }
    }

    public static final JavaInfo CURRENT_ENVIRONMENT =
        new JavaInfo(Platform.CURRENT_PLATFORM, System.getProperty("java.version"), System.getProperty("java.vendor"));

    private final Platform platform;
    private final String version;
    private final @Nullable String vendor;

    private final transient int parsedVersion;
    private final transient VersionNumber versionNumber;

    public JavaInfo(Platform platform, String version, @Nullable String vendor) {
        this.platform = platform;
        this.version = version;
        this.parsedVersion = parseVersion(version);
        this.versionNumber = VersionNumber.asVersion(version);
        this.vendor = vendor;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    public VersionNumber getVersionNumber() {
        return versionNumber;
    }

    public int getParsedVersion() {
        return parsedVersion;
    }

    public @Nullable String getVendor() {
        return vendor;
    }
}
