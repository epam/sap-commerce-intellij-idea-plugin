/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package sap.commerce.toolset.project.descriptor;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class DefaultHybrisProjectDescriptor implements HybrisProjectDescriptor {

    protected final Collection<ModuleDescriptor> foundModules = new ArrayList<>();;
    @NotNull
    protected final List<ModuleDescriptor> modulesChosenForImport = new ArrayList<>();
    private final Set<File> vcs = new HashSet<>();
    private final Set<String> excludedFromScanning = new HashSet<>();
    @Nullable
    protected Project project;
    private final boolean refresh;
    private boolean openProjectSettingsAfterImport;
    private final File rootDirectory;
    @Nullable
    protected File modulesFilesDirectory;
    @Nullable
    protected String ccv2Token;
    @Nullable
    protected File sourceCodeFile;
    @Nullable
    protected File projectIconFile;
    @Nullable
    protected File hybrisDistributionDirectory;
    @Nullable
    protected File externalExtensionsDirectory;
    @Nullable
    protected File externalConfigDirectory;
    @Nullable
    protected File externalDbDriversDirectory;
    @Nullable
    protected String javadocUrl;
    @Nullable
    protected String hybrisVersion;
    private final ProjectImportSettings importContext;

    @NotNull
    private ConfigModuleDescriptor configHybrisModuleDescriptor;
    @NotNull
    private PlatformModuleDescriptor platformHybrisModuleDescriptor;
    @Nullable
    private ModuleDescriptor kotlinNatureModuleDescriptor;

    public DefaultHybrisProjectDescriptor(
        final @NotNull File rootDirectory,
        final @NotNull ProjectImportSettings importContext,
        final boolean refresh,
        final @Nullable Project project
    ) {
        this.rootDirectory = rootDirectory;
        this.refresh = refresh;
        this.importContext = importContext;
        this.project = project;
    }

    @Override
    public void setExcludedFromScanning(final Set<String> excludedFromScanning) {
        this.excludedFromScanning.clear();
        this.excludedFromScanning.addAll(excludedFromScanning);
    }

    @Override
    public Set<String> getExcludedFromScanning() {
        return Collections.unmodifiableSet(excludedFromScanning);
    }

    @Override
    public void setHybrisProject(@Nullable final Project project) {
        this.project = project;
    }

    @Nullable
    @Override
    public Project getProject() {
        return this.project;
    }

    @Override
    public boolean getRefresh() {
        return refresh;
    }

    @Override
    public void setFoundModules(@NotNull final Collection<? extends @NotNull ModuleDescriptor> moduleDescriptors) {
        this.foundModules.clear();
        this.foundModules.addAll(moduleDescriptors);
    }

    @NotNull
    @Override
    public Collection<ModuleDescriptor> getFoundModules() {
        return Collections.unmodifiableCollection(this.foundModules);
    }

    @NotNull
    @Override
    public List<ModuleDescriptor> getChosenModuleDescriptors() {
        return this.modulesChosenForImport;
    }

    @Nullable
    @Override
    public ConfigModuleDescriptor getConfigHybrisModuleDescriptor() {
        return configHybrisModuleDescriptor;
    }

    @NotNull
    @Override
    public PlatformModuleDescriptor getPlatformHybrisModuleDescriptor() {
        return platformHybrisModuleDescriptor;
    }

    @Nullable
    @Override
    public ModuleDescriptor getKotlinNatureModuleDescriptor() {
        return kotlinNatureModuleDescriptor;
    }

    @NotNull
    @Override
    public File getRootDirectory() {
        return this.rootDirectory;
    }

    @Override
    public void clear() {
        this.hybrisDistributionDirectory = null;
        this.externalExtensionsDirectory = null;
        this.externalConfigDirectory = null;
        this.externalDbDriversDirectory = null;
        this.foundModules.clear();
        this.modulesChosenForImport.clear();
        this.vcs.clear();
    }

    @Nullable
    @Override
    public File getModulesFilesDirectory() {
        return this.modulesFilesDirectory;
    }

    @Override
    public void setModulesFilesDirectory(@Nullable final File modulesFilesDirectory) {
        this.modulesFilesDirectory = modulesFilesDirectory;
    }

    @Override
    public @Nullable String getCcv2Token() {
        return ccv2Token;
    }

    @Override
    public void setCcv2Token(@Nullable final String ccv2Token) {
        this.ccv2Token = ccv2Token;
    }

    @Nullable
    @Override
    public File getSourceCodeFile() {
        return sourceCodeFile;
    }

    @Override
    public void setSourceCodeFile(@Nullable final File sourceCodeFile) {
        this.sourceCodeFile = sourceCodeFile;
    }

    @Override
    public @Nullable File getProjectIconFile() {
        return projectIconFile;
    }

    @Override
    public boolean getOpenProjectSettingsAfterImport() {
        return openProjectSettingsAfterImport;
    }

    @Override
    public void setOpenProjectSettingsAfterImport(final boolean openProjectSettingsAfterImport) {
        this.openProjectSettingsAfterImport = openProjectSettingsAfterImport;
    }

    @Override
    public void setProjectIconFile(final File projectIconFile) {
        this.projectIconFile = projectIconFile;
    }

    @Override
    public String getHybrisVersion() {
        return hybrisVersion;
    }

    @Override
    public void setHybrisVersion(final String hybrisVersion) {
        this.hybrisVersion = hybrisVersion;
    }

    @Override
    @Nullable
    public File getHybrisDistributionDirectory() {
        return hybrisDistributionDirectory;
    }

    @Override
    public void setHybrisDistributionDirectory(@Nullable final File hybrisDistributionDirectory) {
        this.hybrisDistributionDirectory = hybrisDistributionDirectory;
    }

    @Override
    @Nullable
    public File getExternalExtensionsDirectory() {
        return externalExtensionsDirectory;
    }

    @Override
    public void setExternalExtensionsDirectory(@Nullable final File externalExtensionsDirectory) {
        this.externalExtensionsDirectory = externalExtensionsDirectory;
    }

    @Override
    @Nullable
    public File getExternalConfigDirectory() {
        return externalConfigDirectory;
    }

    @Override
    public void setExternalConfigDirectory(@Nullable final File externalConfigDirectory) {
        this.externalConfigDirectory = externalConfigDirectory;
    }

    @Override
    @Nullable
    public File getExternalDbDriversDirectory() {
        return externalDbDriversDirectory;
    }

    @Override
    public void setExternalDbDriversDirectory(@Nullable final File externalDbDriversDirectory) {
        this.externalDbDriversDirectory = externalDbDriversDirectory;
    }

    @Nullable
    @Override
    public String getJavadocUrl() {
        return javadocUrl;
    }

    @Override
    public void setJavadocUrl(@Nullable final String javadocUrl) {
        this.javadocUrl = javadocUrl;
    }

    @Override
    public Set<File> getDetectedVcs() {
        return vcs;
    }

    @Override
    public String toString() {
        return "DefaultHybrisProjectDescriptor{" +
            "rootDirectory=" + rootDirectory +
            ", modulesFilesDirectory=" + modulesFilesDirectory +
            ", sourceCodeFile=" + sourceCodeFile +
            ", hybrisDistributionDirectory=" + hybrisDistributionDirectory +
            ", externalExtensionsDirectory=" + externalExtensionsDirectory +
            ", externalConfigDirectory=" + externalConfigDirectory +
            ", externalDbDriversDirectory=" + externalDbDriversDirectory +
            ", javadocUrl='" + javadocUrl + '\'' +
            ", hybrisVersion='" + hybrisVersion + '\'' +
            ", importSettings=" + importContext +
            '}';
    }

    @Override
    public @NotNull ProjectImportSettings getImportContext() {
        return importContext;
    }

    @Override
    public void setConfigHybrisModuleDescriptor(@Nullable final ConfigModuleDescriptor configModuleDescriptor) {
        this.configHybrisModuleDescriptor = configModuleDescriptor;
    }

    @Override
    public void setPlatformHybrisModuleDescriptor(@NotNull final PlatformModuleDescriptor platformModuleDescriptor) {
        this.platformHybrisModuleDescriptor = platformModuleDescriptor;
    }

    @Override
    public void setKotlinNatureModuleDescriptor(@Nullable final ModuleDescriptor moduleDescriptor) {
        this.kotlinNatureModuleDescriptor = moduleDescriptor;
    }
}
