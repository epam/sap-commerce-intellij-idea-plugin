/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.project.context.ProjectImportContext;
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor;
import sap.commerce.toolset.project.descriptor.ModuleDescriptor;
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor;
import sap.commerce.toolset.project.descriptor.impl.YCustomRegularModuleDescriptor;
import sap.commerce.toolset.project.utils.FileUtils;
import sap.commerce.toolset.settings.ApplicationSettings;
import sap.commerce.toolset.util.FileUtilKt;

import java.io.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static sap.commerce.toolset.HybrisConstants.*;

// TODO: -> kotlin
@Deprecated(since = "migrate to kotlin, improve IO operations with NIO")
public final class ModuleGroupingUtil {

    private static final Logger LOG = Logger.getInstance(ModuleGroupingUtil.class);

    private ModuleGroupingUtil() {
    }

    @Nullable
    public static String[] getGroupName(
        final @NotNull ProjectImportContext importContext, @NotNull final ModuleDescriptor moduleDescriptor,
        final Collection<ModuleDescriptor> requiredYModuleDescriptorList
    ) {
        if (!(moduleDescriptor instanceof ConfigModuleDescriptor)) {
            final String[] groupPathOverride = getLocalGroupPathOverride(moduleDescriptor);
            if (groupPathOverride != null) {
                return groupPathOverride.clone();
            }
        }

        final String[] groupPathOverride = getGlobalGroupPathOverride(importContext, moduleDescriptor);
        if (groupPathOverride != null) {
            return groupPathOverride.clone();
        }

        final String[] groupPath = getGroupPath(importContext, moduleDescriptor, requiredYModuleDescriptorList);
        if (groupPath == null) {
            return null;
        }
        return groupPath.clone();
    }

    private static String[] getGlobalGroupPathOverride(final @NotNull ProjectImportContext importContext, final ModuleDescriptor moduleDescriptor) {
        final var configDescriptor = importContext.getConfigModuleDescriptor();
        final var groupFile = configDescriptor.getModuleRootDirectory().resolve(HybrisConstants.IMPORT_OVERRIDE_FILENAME);

        if (!FileUtilKt.getDirectoryExists(groupFile)) {
            createCommentedProperties(groupFile, null, GLOBAL_GROUP_OVERRIDE_COMMENTS);
        }
        return getGroupPathOverride(groupFile, moduleDescriptor);
    }


    private static String[] getLocalGroupPathOverride(final ModuleDescriptor moduleDescriptor) {
        final var groupFile = moduleDescriptor.getModuleRootDirectory().resolve(HybrisConstants.IMPORT_OVERRIDE_FILENAME);
        final var pathOverride = getGroupPathOverride(groupFile, moduleDescriptor);
        if (FileUtilKt.getDirectoryExists(groupFile) && pathOverride == null) {
            createCommentedProperties(groupFile, GROUP_OVERRIDE_KEY, LOCAL_GROUP_OVERRIDE_COMMENTS);
        }
        return pathOverride;
    }

    private static void createCommentedProperties(final Path groupFile, final String key, final String comments) {
        try (final OutputStream out = new FileOutputStream(groupFile.toFile())) {
            final Properties properties = new Properties();
            if (key != null) {
                properties.setProperty(key, "");
            }
            properties.store(out, comments);
        } catch (IOException e) {
            LOG.error("Cannot write " + HybrisConstants.IMPORT_OVERRIDE_FILENAME + ": " + groupFile.toAbsolutePath());
        }
    }

    private static String[] getGroupPathOverride(final Path groupFile, final ModuleDescriptor moduleDescriptor) {
        if (!FileUtilKt.getDirectoryExists(groupFile)) {
            return null;
        }
        // take group override from owner module for sub-modules
        final var moduleName = (moduleDescriptor instanceof final YSubModuleDescriptor subModuleDescriptor)
            ? subModuleDescriptor.getOwner().getName()
            : moduleDescriptor.getName();
        final Properties properties = new Properties();
        try (final InputStream in = new FileInputStream(groupFile.toFile())) {
            properties.load(in);
        } catch (IOException e) {
            LOG.error("Cannot read " + HybrisConstants.IMPORT_OVERRIDE_FILENAME + " for module " + moduleName);
            return null;
        }
        String rawGroupText = properties.getProperty(GROUP_OVERRIDE_KEY);
        if (rawGroupText == null) {
            rawGroupText = properties.getProperty(moduleName + '.' + GROUP_OVERRIDE_KEY);
        }
        return ApplicationSettings.toIdeaGroup(rawGroupText);
    }

    public static String[] getGroupPath(
        final @NotNull ProjectImportContext importContext,
        final @NotNull ModuleDescriptor moduleDescriptor,
        final Collection<ModuleDescriptor> requiredYModuleDescriptorList
    ) {
        final var groupName = moduleDescriptor.groupName(importContext);
        if (groupName != null) return groupName;

        if (moduleDescriptor instanceof final YSubModuleDescriptor ySubModuleDescriptor) {
            return getGroupPath(importContext, ySubModuleDescriptor.getOwner(), requiredYModuleDescriptorList);
        }

        final var platformDirectory = importContext.getPlatformDirectory();
        if (moduleDescriptor instanceof YCustomRegularModuleDescriptor) {
            var customDirectory = importContext.getExternalExtensionsDirectory();

            if (null == customDirectory && platformDirectory != null) {
                customDirectory = platformDirectory.resolve(ProjectConstants.Paths.INSTANCE.getBIN_CUSTOM());
            }
            if (customDirectory == null || !FileUtilKt.getDirectoryExists(customDirectory)) {
                return ApplicationSettings.toIdeaGroup(ApplicationSettings.getInstance().getGroupCustom());
            }

            final List<String> path;
            try {
                path = FileUtils.getPathToParentDirectoryFrom(moduleDescriptor.getModuleRootDirectory().toFile(), customDirectory.toFile());
            } catch (IOException e) {
                LOG.warn(String.format(
                    "Can not build group path for a custom module '%s' because its root directory '%s' is not under" +
                        " custom directory  '%s'.",
                    moduleDescriptor.getName(), moduleDescriptor.getModuleRootDirectory(), customDirectory
                ));
                return ApplicationSettings.toIdeaGroup(ApplicationSettings.getInstance().getGroupCustom());
            }

            final boolean isCustomModuleInLocalExtensionsXml = requiredYModuleDescriptorList.contains(
                moduleDescriptor
            );

            return ArrayUtils.addAll(
                isCustomModuleInLocalExtensionsXml
                    ? ApplicationSettings.toIdeaGroup(ApplicationSettings.getInstance().getGroupCustom())
                    : ApplicationSettings.toIdeaGroup(ApplicationSettings.getInstance().getGroupOtherCustom()),
                path.toArray(new String[0])
            );
        }

        if (requiredYModuleDescriptorList.contains(moduleDescriptor) && platformDirectory != null) {
            final var hybrisBinDirectory = platformDirectory.resolve( ProjectConstants.Directory.BIN);

            final List<String> path;
            try {
                path = FileUtils.getPathToParentDirectoryFrom(moduleDescriptor.getModuleRootDirectory().toFile(), hybrisBinDirectory.toFile());
            } catch (final IOException e) {
                LOG.warn(String.format(
                    "Can not build group path for OOTB module '%s' because its root directory '%s' is not under Hybris bin directory '%s'.",
                    moduleDescriptor.getName(), moduleDescriptor.getModuleRootDirectory(), hybrisBinDirectory
                ));
                return ApplicationSettings.toIdeaGroup(ApplicationSettings.getInstance().getGroupHybris());
            }

            if (!path.isEmpty() && path.getFirst().equals("modules")) {
                path.removeFirst();
            }
            return ArrayUtils.addAll(ApplicationSettings.toIdeaGroup(ApplicationSettings.getInstance().getGroupHybris()), path.toArray(new String[0]));
        }

        return ApplicationSettings.toIdeaGroup(ApplicationSettings.getInstance().getGroupOtherHybris());
    }
}
