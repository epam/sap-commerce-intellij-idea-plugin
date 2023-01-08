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

package com.intellij.idea.plugin.hybris.settings;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created 6:51 PM 28 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class HybrisProjectSettings {

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
    protected boolean importOotbModulesInReadOnlyMode;
    protected boolean followSymlink;
    protected boolean scanThroughExternalModule;
    protected boolean createBackwardCyclicDependenciesForAddOns = false;
    protected boolean excludeTestSources;
    protected Set<String> completeSetOfAvailableExtensionsInHybris = new HashSet<>();
    protected Set<String> unusedExtensions = new HashSet<>();
    protected Set<String> modulesOnBlackList = new HashSet<>();
    protected Map<String, ModuleSettings> moduleSettings = new HashMap<>();

    public boolean isHybrisProject() {
        return hybrisProject;
    }

    public void setHybrisProject(final boolean hybrisProject) {
        this.hybrisProject = hybrisProject;
    }

    public String getCustomDirectory() {
        return customDirectory;
    }

    public void setCustomDirectory(final String customDirectory) {
        this.customDirectory = customDirectory;
    }

    public String getConfigDirectory() {
        return configDirectory;
    }

    public void setConfigDirectory(final String configDirectory) {
        this.configDirectory = configDirectory;
    }

    public String getHybrisDirectory() {
        return hybrisDirectory;
    }

    public void setHybrisDirectory(final String hybrisDirectory) {
        this.hybrisDirectory = hybrisDirectory;
    }

    public String getImportedByVersion() {
        return importedByVersion;
    }

    public void setImportedByVersion(final String importedByVersion) {
        this.importedByVersion = importedByVersion;
    }

    public String getHybrisVersion() {
        return hybrisVersion;
    }

    public void setHybrisVersion(final String hybrisVersion) {
        this.hybrisVersion = hybrisVersion;
    }

    public String getJavadocUrl() {
        return javadocUrl;
    }

    public void setJavadocUrl(final String javadocUrl) {
        this.javadocUrl = javadocUrl;
    }

    public String getSourceCodeFile() {
        return sourceCodeFile;
    }

    public void setSourceCodeFile(final String sourceCodeFile) {
        this.sourceCodeFile = sourceCodeFile;
    }

    public String getExternalExtensionsDirectory() {
        return externalExtensionsDirectory;
    }

    public void setExternalExtensionsDirectory(final String externalExtensionsDirectory) {
        this.externalExtensionsDirectory = externalExtensionsDirectory;
    }

    public String getExternalConfigDirectory() {
        return externalConfigDirectory;
    }

    public void setExternalConfigDirectory(final String externalConfigDirectory) {
        this.externalConfigDirectory = externalConfigDirectory;
    }

    public String getExternalDbDriversDirectory() {
        return externalDbDriversDirectory;
    }

    public void setExternalDbDriversDirectory(final String externalDbDriversDirectory) {
        this.externalDbDriversDirectory = externalDbDriversDirectory;
    }

    public String getIdeModulesFilesDirectory() {
        return ideModulesFilesDirectory;
    }

    public void setIdeModulesFilesDirectory(final String ideModulesFilesDirectory) {
        this.ideModulesFilesDirectory = ideModulesFilesDirectory;
    }

    public boolean isImportOotbModulesInReadOnlyMode() {
        return importOotbModulesInReadOnlyMode;
    }

    public void setImportOotbModulesInReadOnlyMode(final boolean importOotbModulesInReadOnlyMode) {
        this.importOotbModulesInReadOnlyMode = importOotbModulesInReadOnlyMode;
    }

    public boolean isFollowSymlink() {
        return followSymlink;
    }

    public void setFollowSymlink(final boolean followSymlink) {
        this.followSymlink = followSymlink;
    }

    public boolean isScanThroughExternalModule() {
        return scanThroughExternalModule;
    }

    public void setScanThroughExternalModule(final boolean scanThroughExternalModule) {
        this.scanThroughExternalModule = scanThroughExternalModule;
    }

    public boolean isExcludeTestSources() {
        return excludeTestSources;
    }

    public void setExcludeTestSources(final boolean excludeTestSources) {
        this.excludeTestSources = excludeTestSources;
    }

    public Set<String> getCompleteSetOfAvailableExtensionsInHybris() {
        return completeSetOfAvailableExtensionsInHybris;
    }

    public void setCompleteSetOfAvailableExtensionsInHybris(final Set<String> completeSetOfAvailableExtensionsInHybris) {
        this.completeSetOfAvailableExtensionsInHybris = completeSetOfAvailableExtensionsInHybris;
    }

    public boolean isCreateBackwardCyclicDependenciesForAddOns() {
        return createBackwardCyclicDependenciesForAddOns;
    }

    public void setCreateBackwardCyclicDependenciesForAddOns(final boolean createBackwardCyclicDependenciesForAddOns) {
        this.createBackwardCyclicDependenciesForAddOns = createBackwardCyclicDependenciesForAddOns;
    }

    public Set<String> getUnusedExtensions() {
        return unusedExtensions;
    }

    public void setUnusedExtensions(final Set<String> unusedExtensions) {
        this.unusedExtensions = unusedExtensions;
    }

    public void setModulesOnBlackList(final Set<String> modulesOnBlackList) {
        this.modulesOnBlackList = modulesOnBlackList;
    }

    public Set<String> getModulesOnBlackList() {
        return modulesOnBlackList;
    }

    public Map<String, ModuleSettings> getModuleSettings() {
        return moduleSettings;
    }

    public void setModuleSettings(final Map<String, ModuleSettings> moduleSettings) {
        this.moduleSettings = moduleSettings;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(hybrisProject)
            .append(customDirectory)
            .append(hybrisDirectory)
            .append(configDirectory)
            .append(importedByVersion)
            .append(hybrisVersion)
            .append(javadocUrl)
            .append(sourceCodeFile)
            .append(externalExtensionsDirectory)
            .append(externalConfigDirectory)
            .append(externalDbDriversDirectory)
            .append(ideModulesFilesDirectory)
            .append(importOotbModulesInReadOnlyMode)
            .append(followSymlink)
            .append(excludeTestSources)
            .append(scanThroughExternalModule)
            .append(completeSetOfAvailableExtensionsInHybris)
            .append(createBackwardCyclicDependenciesForAddOns)
            .append(unusedExtensions)
            .append(moduleSettings)
            .toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (null == o || getClass() != o.getClass()) {
            return false;
        }

        final HybrisProjectSettings other = (HybrisProjectSettings) o;

        return new EqualsBuilder()
            .append(hybrisProject, other.hybrisProject)
            .append(customDirectory, other.customDirectory)
            .append(hybrisDirectory, other.hybrisDirectory)
            .append(configDirectory, other.configDirectory)
            .append(importedByVersion, other.importedByVersion)
            .append(hybrisVersion, other.hybrisVersion)
            .append(javadocUrl, other.javadocUrl)
            .append(sourceCodeFile, other.sourceCodeFile)
            .append(externalExtensionsDirectory, other.externalExtensionsDirectory)
            .append(externalConfigDirectory, other.externalConfigDirectory)
            .append(externalDbDriversDirectory, other.externalDbDriversDirectory)
            .append(ideModulesFilesDirectory, other.ideModulesFilesDirectory)
            .append(importOotbModulesInReadOnlyMode, other.importOotbModulesInReadOnlyMode)
            .append(followSymlink, other.followSymlink)
            .append(excludeTestSources, other.excludeTestSources)
            .append(scanThroughExternalModule, other.scanThroughExternalModule)
            .append(completeSetOfAvailableExtensionsInHybris, other.completeSetOfAvailableExtensionsInHybris)
            .append(createBackwardCyclicDependenciesForAddOns, other.createBackwardCyclicDependenciesForAddOns)
            .append(unusedExtensions, other.unusedExtensions)
            .append(moduleSettings, other.moduleSettings)
            .isEquals();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HybrisProjectSettings{");
        sb.append("hybrisProject=").append(hybrisProject);
        sb.append("customDirectoryPath=").append(customDirectory);
        sb.append("hybrisDirectory=").append(hybrisDirectory);
        sb.append("configDirectory=").append(configDirectory);
        sb.append("importedByVersion=").append(importedByVersion);
        sb.append("hybrisVersion=").append(hybrisVersion);
        sb.append("javadocUrl=").append(javadocUrl);
        sb.append("sourceCodeFile=").append(sourceCodeFile);
        sb.append("externalExtensionsDirectory=").append(externalExtensionsDirectory);
        sb.append("externalConfigDirectory=").append(externalConfigDirectory);
        sb.append("externalDbDriversDirectory=").append(externalDbDriversDirectory);
        sb.append("ideModulesFilesDirectory=").append(ideModulesFilesDirectory);
        sb.append("importOotbModulesInReadOnlyMode=").append(importOotbModulesInReadOnlyMode);
        sb.append("followSymlink=").append(followSymlink);
        sb.append("excludeTestSources=").append(excludeTestSources);
        sb.append("scanThroughExternalModule=").append(scanThroughExternalModule);
        sb.append("completeSetOfAvailableExtensionsInHybris=").append(completeSetOfAvailableExtensionsInHybris);
        sb.append("createBackwardCyclicDependenciesForAddOns=").append(createBackwardCyclicDependenciesForAddOns);
        sb.append("unusedExtensions=").append(unusedExtensions);
        sb.append("moduleSettings=").append(moduleSettings);
        sb.append('}');
        return sb.toString();
    }

    public static class ModuleSettings {
        boolean readonly;
        String descriptorType;

        public boolean isReadonly() {
            return readonly;
        }

        public void setReadonly(final boolean readonly) {
            this.readonly = readonly;
        }

        public String getDescriptorType() {
            return descriptorType;
        }

        public void setDescriptorType(final String descriptorType) {
            this.descriptorType = descriptorType;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof final ModuleSettings other)) {
                return false;
            }

            if (readonly != other.readonly) {
                return false;
            }
            return Objects.equals(descriptorType, other.descriptorType);
        }

        @Override
        public int hashCode() {
            int result = (readonly ? 1 : 0);
            result = 31 * result + (descriptorType != null ? descriptorType.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return new StringBuilder("ModuleSettings{")
                .append("readonly=").append(readonly)
                .append(", descriptorType='").append(descriptorType).append('\'')
                .append('}')
                .toString();
        }
    }
}
