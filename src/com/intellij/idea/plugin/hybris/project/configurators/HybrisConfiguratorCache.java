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

package com.intellij.idea.plugin.hybris.project.configurators;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HybrisConfiguratorCache {

    private static final Logger LOG = Logger.getInstance(HybrisConfiguratorCache.class);

    private final Map<String, Ref<Properties>> path2Properties = new HashMap<>();

    @Nullable
    private Properties getParsedProperties(@NotNull final String filePath) {
        final Ref<Properties> propertiesRef = path2Properties.get(FileUtil.toSystemIndependentName(filePath));

        if (propertiesRef != null) {
            return propertiesRef.get();
        }
        final File file = new File(filePath);

        if (!file.exists()) {
            return null;
        }
        Properties properties = new Properties();

        try (final FileReader fr = new FileReader(file)) {
            properties.load(fr);
        } catch (IOException e) {
            LOG.debug(e);
            properties = null;
        }
        path2Properties.put(filePath, Ref.create(properties));
        return properties;
    }

    @Nullable
    public String findPropertyInFiles(@NotNull final Iterable<File> files, @NotNull final String propertyName) {
        for (File file : files) {
            final String candidate = findPropertyInFile(file, propertyName);

            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    @Nullable
    public String findPropertyInFile(@NotNull final File file, @NotNull final String propertyName) {
        final Properties properties = getParsedProperties(file.getPath());

        if (properties == null) {
            return null;
        }
        return (String) properties.get(propertyName);
    }
}
