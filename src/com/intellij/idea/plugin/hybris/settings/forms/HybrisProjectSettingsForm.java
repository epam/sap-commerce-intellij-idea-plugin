/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.settings.forms;

import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;

import javax.swing.*;

public class HybrisProjectSettingsForm {

    private JPanel mainPanel;
    private JCheckBox createBackwardCyclicDependenciesForAddons;
    private JCheckBox importOotbModulesInReadOnlyMode;
    private JCheckBox followSymlink;
    private JCheckBox scanThroughExternalModule;
    private JCheckBox excludeTestSources;

    /*
    protected boolean hybrisProject;
    protected String customDirectory;
    protected String hybrisDirectory;
    protected String configDirectory;
    protected String importedByVersion;
    protected String hybrisVersion;
    protected String javadocUrl;
    protected String sourceCodeFile;
    protected String externalExtensionsDirectory;
    protected String externalConfigDirectory;
    protected String externalDbDriversDirectory;
    protected String ideModulesFilesDirectory;
    protected Set<String> completeSetOfAvailableExtensionsInHybris = new HashSet<>();
    protected Set<String> unusedExtensions = new HashSet<>();
    protected Set<String> modulesOnBlackList = new HashSet<>();
     */


    public void setData(final HybrisProjectSettings data) {
        createBackwardCyclicDependenciesForAddons.setSelected(data.isCreateBackwardCyclicDependenciesForAddOns());
        followSymlink.setSelected(data.isFollowSymlink());
        scanThroughExternalModule.setSelected(data.isScanThroughExternalModule());
        excludeTestSources.setSelected(data.isExcludeTestSources());
        importOotbModulesInReadOnlyMode.setSelected(data.isImportOotbModulesInReadOnlyMode());
    }

    public void getData(final HybrisProjectSettings data) {
        data.setCreateBackwardCyclicDependenciesForAddOns(createBackwardCyclicDependenciesForAddons.isSelected());
        data.setFollowSymlink(followSymlink.isSelected());
        data.setScanThroughExternalModule(scanThroughExternalModule.isSelected());
        data.setExcludeTestSources(excludeTestSources.isSelected());
        data.setImportOotbModulesInReadOnlyMode(importOotbModulesInReadOnlyMode.isSelected());
    }

    public boolean isModified(final HybrisProjectSettings data) {
        if (createBackwardCyclicDependenciesForAddons.isSelected() != data.isCreateBackwardCyclicDependenciesForAddOns()) {
            return true;
        }
        if (importOotbModulesInReadOnlyMode.isSelected() != data.isImportOotbModulesInReadOnlyMode()) {
            return true;
        }
        if (scanThroughExternalModule.isSelected() != data.isScanThroughExternalModule()) {
            return true;
        }
        if (followSymlink.isSelected() != data.isFollowSymlink()) {
            return true;
        }
        return false;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

}
