/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.project.utils;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FileUtils {
    private static final Logger LOG = Logger.getInstance(FileUtils.class);

    private FileUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Util class should never be instantiated.");
    }

    public static boolean isFileUnder(@NotNull final File child, @NotNull final File parent) {
        final String parentPath = FileUtil.toCanonicalPath(parent.getPath())+ '/';
        final String childPath = FileUtil.toCanonicalPath(child.getPath())+ '/';

        return childPath.startsWith(parentPath);
    }

    @NotNull
    public static List<String> getPathToParentDirectoryFrom(
        @NotNull final File file,
        @NotNull final File parentDirectory
    ) throws IOException {
        if (!FileUtils.isFileUnder(file, parentDirectory)) {
            throw new IOException("File '" + file + "' is not under '" + parentDirectory + '\'');
        }

        final List<String> path = new ArrayList<>();
        File currentDirectory = file.getParentFile();

        while (currentDirectory != null && !FileUtil.filesEqual(currentDirectory, parentDirectory)) {
            path.add(currentDirectory.getName());
            currentDirectory = currentDirectory.getParentFile();
        }

        final List<String> reversePath = Lists.reverse(path);
        LOG.info("Relative path for module dir " + file.getAbsolutePath() + " in " + parentDirectory.getAbsolutePath()
                 + " found as " + StringUtils.join(reversePath, '/'));
        return reversePath;
    }

    public static @Nullable File toFile(final String path) {
        return toFile(path, false);
    }

    public static @Nullable File toFile(String path, final boolean checkExists) {
        if (path == null) {
            return null;
        }
        // this does not expand symlinks
        path = FileUtil.toCanonicalPath(path);
        final File file = new File(path);
        if (checkExists && !file.exists()) {
            return null;
        }
        return file;
    }

    public static File toFile(final String parent, final String child) {
        final File file = new File(parent, child);
        return toFile(file.getPath(), false);
    }
}
