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
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor;
import sap.commerce.toolset.project.descriptor.ModuleDescriptor;
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor;
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor;
import sap.commerce.toolset.project.descriptor.impl.*;
import sap.commerce.toolset.project.utils.FileUtils;
import sap.commerce.toolset.settings.ApplicationSettings;

import java.io.*;
import java.util.*;

import static sap.commerce.toolset.HybrisConstants.*;
import static sap.commerce.toolset.project.utils.FileUtils.toFile;

public final class ModuleGroupingUtil {

    private static final Logger LOG = Logger.getInstance(ModuleGroupingUtil.class);

    public static Map<String, String[]> getPredefinedGroups(final ApplicationSettings applicationSettings) {
        // TODO, fix the map, it cannot have null values
        return Map.of(
            "groupCustom", ApplicationSettings.toIdeaGroup(applicationSettings.getGroupCustom()),
            "groupNonHybris", ApplicationSettings.toIdeaGroup(applicationSettings.getGroupNonHybris()),
            "groupOtherCustom", ApplicationSettings.toIdeaGroup(applicationSettings.getGroupOtherCustom()),
            "groupHybris", ApplicationSettings.toIdeaGroup(applicationSettings.getGroupHybris()),
            "groupOtherHybris", ApplicationSettings.toIdeaGroup(applicationSettings.getGroupOtherHybris()),
            "groupPlatform", ApplicationSettings.toIdeaGroup(applicationSettings.getGroupPlatform()),
            "groupCCv2", ApplicationSettings.toIdeaGroup(applicationSettings.getGroupCCv2())
        );
    }

    @Nullable
    public static String[] getGroupName(@NotNull final ModuleDescriptor moduleDescriptor, final Collection<ModuleDescriptor> requiredYModuleDescriptorList, final Map<String, String[]> groups) {
        if (!(moduleDescriptor instanceof ConfigModuleDescriptor)) {
            final String[] groupPathOverride = getLocalGroupPathOverride(moduleDescriptor);
            if (groupPathOverride != null) {
                return groupPathOverride.clone();
            }
        }

        final String[] groupPathOverride = getGlobalGroupPathOverride(moduleDescriptor);
        if (groupPathOverride != null) {
            return groupPathOverride.clone();
        }

        final String[] groupPath = getGroupPath(moduleDescriptor, requiredYModuleDescriptorList, groups);
        if (groupPath == null) {
            return null;
        }
        return groupPath.clone();
    }

    private static String[] getGlobalGroupPathOverride(final ModuleDescriptor moduleDescriptor) {
        final ConfigModuleDescriptor configDescriptor = moduleDescriptor.getRootProjectDescriptor().getConfigHybrisModuleDescriptor();
        if (configDescriptor == null) {
            return null;
        }
        final File groupFile = new File(configDescriptor.getModuleRootDirectory(), HybrisConstants.IMPORT_OVERRIDE_FILENAME);
        if (!groupFile.exists()) {
            createCommentedProperties(groupFile, null, GLOBAL_GROUP_OVERRIDE_COMMENTS);
        }
        return getGroupPathOverride(groupFile, moduleDescriptor);
    }


    private static String[] getLocalGroupPathOverride(final ModuleDescriptor moduleDescriptor) {
        final File groupFile = new File(moduleDescriptor.getModuleRootDirectory(), HybrisConstants.IMPORT_OVERRIDE_FILENAME);
        final String[] pathOverride = getGroupPathOverride(groupFile, moduleDescriptor);
        if (groupFile.exists() && pathOverride == null) {
            createCommentedProperties(groupFile, GROUP_OVERRIDE_KEY, LOCAL_GROUP_OVERRIDE_COMMENTS);
        }
        return pathOverride;
    }

    private static void createCommentedProperties(final File groupFile, final String key, final String comments) {
        try (final OutputStream out = new FileOutputStream(groupFile)) {
            final Properties properties = new Properties();
            if (key != null) {
                properties.setProperty(key, "");
            }
            properties.store(out, comments);
        } catch (IOException e) {
            LOG.error("Cannot write " + HybrisConstants.IMPORT_OVERRIDE_FILENAME + ": " + groupFile.getAbsolutePath());
        }
    }

    private static String[] getGroupPathOverride(final File groupFile, final ModuleDescriptor moduleDescriptor) {
        if (!groupFile.exists()) {
            return null;
        }
        // take group override from owner module for sub-modules
        final var moduleName = (moduleDescriptor instanceof final YSubModuleDescriptor subModuleDescriptor)
            ? subModuleDescriptor.getOwner().getName()
            : moduleDescriptor.getName();
        final Properties properties = new Properties();
        try (final InputStream in = new FileInputStream(groupFile)) {
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

    private static String[] getGroupPath(@NotNull final ModuleDescriptor moduleDescriptor, final Collection<ModuleDescriptor> requiredYModuleDescriptorList, final Map<String, String[]> groups) {
        if (moduleDescriptor instanceof final YSubModuleDescriptor ySubModuleDescriptor) {
            return getGroupPath(ySubModuleDescriptor.getOwner(), requiredYModuleDescriptorList, groups);
        }

        if (moduleDescriptor instanceof CCv2ModuleDescriptor) {
            return groups.get("groupCCv2");
        }

        if (moduleDescriptor instanceof AngularModuleDescriptor && requiredYModuleDescriptorList.size() == 1) {
            // assumption that there can be only 1 parent
            final var parent = requiredYModuleDescriptorList.iterator().next();
            final var parentPath = getGroupPath(parent, List.of(), groups);
            final var completeGroup = Arrays.copyOf(parentPath, parentPath.length + 1);
            completeGroup[completeGroup.length - 1] = parent.getName();

            return completeGroup;
        }

        if (moduleDescriptor instanceof PlatformModuleDescriptor) {
            return groups.get("groupPlatform");
        }

        if (moduleDescriptor instanceof YPlatformExtModuleDescriptor) {
            return groups.get("groupPlatform");
        }

        if (moduleDescriptor instanceof ConfigModuleDescriptor) {
            return groups.get("groupCustom");
        }

        if (moduleDescriptor instanceof ExternalModuleDescriptor) {
            return groups.get("groupNonHybris");
        }

        if (moduleDescriptor instanceof YCustomRegularModuleDescriptor) {
            File customDirectory = moduleDescriptor.getRootProjectDescriptor().getExternalExtensionsDirectory();

            if (null == customDirectory) {
                customDirectory = new File(moduleDescriptor.getRootProjectDescriptor().getHybrisDistributionDirectory(), HybrisConstants.CUSTOM_MODULES_DIRECTORY_RELATIVE_PATH);
            }
            if (!customDirectory.exists()) {
                return groups.get("groupCustom");
            }
            customDirectory = toFile(customDirectory.getAbsolutePath());

            final List<String> path;
            try {
                path = FileUtils.getPathToParentDirectoryFrom(moduleDescriptor.getModuleRootDirectory(), customDirectory);
            } catch (IOException e) {
                LOG.warn(String.format(
                    "Can not build group path for a custom module '%s' because its root directory '%s' is not under" +
                        " custom directory  '%s'.",
                    moduleDescriptor.getName(), moduleDescriptor.getModuleRootDirectory(), customDirectory
                ));
                return groups.get("groupCustom");
            }

            final boolean isCustomModuleInLocalExtensionsXml = requiredYModuleDescriptorList.contains(
                moduleDescriptor
            );

            return ArrayUtils.addAll(
                isCustomModuleInLocalExtensionsXml ? groups.get("groupCustom") : groups.get("groupOtherCustom"),
                path.toArray(new String[0])
            );
        }

        if (requiredYModuleDescriptorList.contains(moduleDescriptor)) {
            final File hybrisBinDirectory = new File(
                moduleDescriptor.getRootProjectDescriptor().getHybrisDistributionDirectory(),
                HybrisConstants.BIN_DIRECTORY
            );

            final List<String> path;
            try {
                path = FileUtils.getPathToParentDirectoryFrom(moduleDescriptor.getModuleRootDirectory(), hybrisBinDirectory);
            } catch (IOException e) {
                LOG.warn(String.format(
                    "Can not build group path for OOTB module '%s' because its root directory '%s' is not under Hybris bin directory '%s'.",
                    moduleDescriptor.getName(), moduleDescriptor.getModuleRootDirectory(), hybrisBinDirectory
                ));
                return groups.get("groupHybris");
            }

            if (!path.isEmpty() && path.get(0).equals("modules")) {
                path.remove(0);
            }
            return ArrayUtils.addAll(groups.get("groupHybris"), path.toArray(new String[0]));
        }

        return groups.get("groupOtherHybris");
    }
}
