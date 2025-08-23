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

package sap.commerce.toolset.project.configurator;

import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.project.ModuleGroupingUtil;
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor;
import sap.commerce.toolset.project.descriptor.ModuleDescriptor;
import sap.commerce.toolset.project.descriptor.YModuleDescriptor;
import sap.commerce.toolset.settings.ApplicationSettings;

import java.util.HashSet;
import java.util.Map;

import static sap.commerce.toolset.HybrisI18NBundleUtils.message;

public class GroupModuleConfigurator implements ProjectPreImportConfigurator {

    @Override
    public void preConfigure(
        @NotNull final ProgressIndicator indicator,
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final Map<@NotNull String, ? extends @NotNull ModuleDescriptor> moduleDescriptors
    ) {
        indicator.setText2(message("hybris.project.import.module.groups"));
        final var applicationSettings = ApplicationSettings.getInstance();
        if (!applicationSettings.getGroupModules()) {
            return;
        }
        final var requiredYModuleDescriptorList = new HashSet<ModuleDescriptor>();

        final var moduleDescriptorsToImport = hybrisProjectDescriptor.getModulesChosenForImport();
        moduleDescriptorsToImport.stream()
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .filter(ModuleDescriptor::isPreselected)
            .forEach(it -> {
                requiredYModuleDescriptorList.add(it);
                requiredYModuleDescriptorList.addAll(it.getAllDependencies());
            });

        moduleDescriptorsToImport.forEach(it -> {
            final var groupNames = ModuleGroupingUtil.getGroupName(it, requiredYModuleDescriptorList);
            if (groupNames != null) {
                it.setGroupNames(groupNames);
            }
        });

        indicator.setText2("");
    }
}
