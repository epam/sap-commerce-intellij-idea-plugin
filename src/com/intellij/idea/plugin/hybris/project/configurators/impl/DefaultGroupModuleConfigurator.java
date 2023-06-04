/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.GroupModuleConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.*;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*;
import com.intellij.idea.plugin.hybris.project.utils.FileUtils;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.*;
import static com.intellij.idea.plugin.hybris.project.utils.FileUtils.toFile;

public class DefaultGroupModuleConfigurator implements GroupModuleConfigurator {
    private static final Logger LOG = Logger.getInstance(DefaultGroupModuleConfigurator.class);

    private Set<ModuleDescriptor> requiredYModuleDescriptorList;
    private boolean groupModules;
    private String[] groupCustom;
    private String[] groupNonHybris;
    private String[] groupOtherCustom;
    private String[] groupHybris;
    private String[] groupPlatform;
    private String[] groupOtherHybris;
    private String[] groupCCv2;

    public DefaultGroupModuleConfigurator() {
        readSettings();
    }

    @Override
    public void findDependencyModules(@NotNull final List<ModuleDescriptor> modulesChosenForImport) {
        readSettings();
        if (!groupModules) {
            return;
        }
        requiredYModuleDescriptorList = new HashSet<>();

        modulesChosenForImport.stream()
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .filter(YModuleDescriptorUtil.INSTANCE::isPreselected)
            .forEach(it -> {
                requiredYModuleDescriptorList.add(it);
                requiredYModuleDescriptorList.addAll(YModuleDescriptorUtil.INSTANCE.getDependenciesPlainList(it));
            });
    }

    @Override
    public void configure(
        @NotNull final ModifiableModuleModel modifiableModuleModel,
        @NotNull final Module module,
        @NotNull final ModuleDescriptor moduleDescriptor
    ) {
        if (!groupModules) {
            return;
        }
        String[] groupNamePath = getGroupName(moduleDescriptor);
        modifiableModuleModel.setModuleGroupPath(module, groupNamePath);
    }

    @Nullable
    @Override
    public String[] getGroupName(@NotNull final ModuleDescriptor moduleDescriptor) {
        if (!(moduleDescriptor instanceof YConfigModuleDescriptor)) {
            final String[] groupPathOverride = getLocalGroupPathOverride(moduleDescriptor);
            if (groupPathOverride != null) {
                return groupPathOverride.clone();
            }
        }

        final String[] groupPathOverride = getGlobalGroupPathOverride(moduleDescriptor);
        if (groupPathOverride != null) {
            return groupPathOverride.clone();
        }

        String[] groupPath = getGroupPath(moduleDescriptor);
        if (groupPath == null) {
            return null;
        }
        return groupPath.clone();
    }

    private String[] getGlobalGroupPathOverride(final ModuleDescriptor moduleDescriptor) {
        final YConfigModuleDescriptor configDescriptor = moduleDescriptor.getRootProjectDescriptor().getConfigHybrisModuleDescriptor();
        if (configDescriptor == null) {
            return null;
        }
        final File groupFile = new File(configDescriptor.getRootDirectory(), HybrisConstants.IMPORT_OVERRIDE_FILENAME);
        if (!groupFile.exists()) {
            createCommentedProperties(groupFile, null, GLOBAL_GROUP_OVERRIDE_COMMENTS);
        }
        return getGroupPathOverride(groupFile, moduleDescriptor.getName());
    }


    private String[] getLocalGroupPathOverride(final ModuleDescriptor moduleDescriptor) {
        final File groupFile = new File(moduleDescriptor.getRootDirectory(), HybrisConstants.IMPORT_OVERRIDE_FILENAME);
        final String[] pathOverride = getGroupPathOverride(groupFile, moduleDescriptor.getName());
        if (groupFile.exists() && pathOverride == null) {
            createCommentedProperties(groupFile, GROUP_OVERRIDE_KEY, LOCAL_GROUP_OVERRIDE_COMMENTS);
        }
        return pathOverride;
    }

    private void createCommentedProperties(final File groupFile, final String key, final String comments) {
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

    private String[] getGroupPathOverride(final File groupFile, final String moduleName) {
        if (!groupFile.exists()) {
            return null;
        }
        String rawGroupText = null;
        final Properties properties = new Properties();
        try (final InputStream in = new FileInputStream(groupFile)) {
            properties.load(in);
        } catch (IOException e) {
            LOG.error("Cannot read " + HybrisConstants.IMPORT_OVERRIDE_FILENAME + " for module " + moduleName);
            return null;
        }
        rawGroupText = properties.getProperty(GROUP_OVERRIDE_KEY);
        if (rawGroupText == null) {
            rawGroupText = properties.getProperty(moduleName + '.' + GROUP_OVERRIDE_KEY);
        }
        return HybrisApplicationSettingsComponent.toIdeaGroup(rawGroupText);
    }

    private String[] getGroupPath(@NotNull final ModuleDescriptor moduleDescriptor) {
        if (moduleDescriptor instanceof final YSubModuleDescriptor ySubModuleDescriptor) {
            return getGroupPath(ySubModuleDescriptor.getOwner());
        }

        if (moduleDescriptor instanceof CCv2ModuleDescriptor) {
            return groupCCv2;
        }

        if (moduleDescriptor instanceof YPlatformModuleDescriptor) {
            return groupPlatform;
        }

        if (moduleDescriptor instanceof YPlatformExtModuleDescriptor) {
            return groupPlatform;
        }

        if (moduleDescriptor instanceof YConfigModuleDescriptor) {
            return groupCustom;
        }

        if (moduleDescriptor instanceof RootModuleDescriptor) {
            return groupNonHybris;
        }

        if (moduleDescriptor instanceof YCustomRegularModuleDescriptor) {
            File customDirectory = moduleDescriptor.getRootProjectDescriptor().getExternalExtensionsDirectory();

            if (null == customDirectory) {
                customDirectory = new File(moduleDescriptor.getRootProjectDescriptor().getHybrisDistributionDirectory(), HybrisConstants.CUSTOM_MODULES_DIRECTORY_RELATIVE_PATH);
            }
            if (!customDirectory.exists()) {
                return this.groupCustom;
            }
            customDirectory = toFile(customDirectory.getAbsolutePath());

            final List<String> path;
            try {
                path = FileUtils.getPathToParentDirectoryFrom(moduleDescriptor.getRootDirectory(), customDirectory);
            } catch (IOException e) {
                LOG.warn(String.format(
                    "Can not build group path for a custom module '%s' because its root directory '%s' is not under" +
                        " custom directory  '%s'.",
                    moduleDescriptor.getName(), moduleDescriptor.getRootDirectory(), customDirectory
                ));
                return this.groupCustom;
            }

            final boolean isCustomModuleInLocalExtensionsXml = this.requiredYModuleDescriptorList.contains(
                moduleDescriptor
            );

            return ArrayUtils.addAll(
                isCustomModuleInLocalExtensionsXml ? this.groupCustom : this.groupOtherCustom,
                path.toArray(new String[0])
            );
        }

        if (this.requiredYModuleDescriptorList.contains(moduleDescriptor)) {
            final File hybrisBinDirectory = new File(
                moduleDescriptor.getRootProjectDescriptor().getHybrisDistributionDirectory(),
                HybrisConstants.BIN_DIRECTORY
            );

            final List<String> path;
            try {
                path = FileUtils.getPathToParentDirectoryFrom(moduleDescriptor.getRootDirectory(), hybrisBinDirectory);
            } catch (IOException e) {
                LOG.warn(String.format(
                    "Can not build group path for OOTB module '%s' because its root directory '%s' is not under" +
                        "under Hybris bin directory  '%s'.",
                    moduleDescriptor.getName(), moduleDescriptor.getRootDirectory(), hybrisBinDirectory
                ));
                return this.groupHybris;
            }

            if (!path.isEmpty() && path.get(0).equals("modules")) {
                path.remove(0);
            }
            return ArrayUtils.addAll(this.groupHybris, path.toArray(new String[0]));
        }

        return groupOtherHybris;
    }

    private void readSettings() {
        final HybrisApplicationSettings hybrisApplicationSettings = HybrisApplicationSettingsComponent.getInstance()
            .getState();
        groupModules = hybrisApplicationSettings.getGroupModules();
        groupCustom = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupCustom());
        groupNonHybris = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupNonHybris());
        groupOtherCustom = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupOtherCustom());
        groupHybris = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupHybris());
        groupOtherHybris = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupOtherHybris());
        groupPlatform = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupPlatform());
        groupCCv2 = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.getGroupCCv2());
    }

}
