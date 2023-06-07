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

package com.intellij.idea.plugin.hybris.project.descriptors;

import com.google.common.collect.Sets;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.LibraryDescriptorType;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ExtensionInfo;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.MetaType;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.RequiresExtensionType;
import com.intellij.idea.plugin.hybris.settings.ExtensionDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.project.Project;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.*;
import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType.CUSTOM;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public abstract class RegularHybrisModuleDescriptor extends AbstractHybrisModuleDescriptor {

    @NotNull
    protected final ExtensionInfo extensionInfo;
    private final Map<String, String> metas;

    protected RegularHybrisModuleDescriptor(
        @NotNull final File moduleRootDirectory,
        @NotNull final HybrisProjectDescriptor rootProjectDescriptor,
        @NotNull final ExtensionInfo extensionInfo
    ) throws HybrisConfigurationException {
        super(moduleRootDirectory, rootProjectDescriptor, extensionInfo.getExtension().getName());

        this.extensionInfo = extensionInfo;
        this.metas = CollectionUtils.emptyIfNull(extensionInfo.getExtension().getMeta()).stream()
            .collect(Collectors.toMap(MetaType::getKey, MetaType::getValue));
    }

    @Nullable
    @Override
    public File getWebRoot() {

        final File webRoot = new File(this.getRootDirectory(), HybrisConstants.WEB_ROOT_DIRECTORY_RELATIVE_PATH);

        if (webRoot.exists()) {
            return webRoot;
        }

        return null;
    }

    @NotNull
    @Override
    public Set<String> getRequiredExtensionNames() {
        if (null == this.extensionInfo.getExtension()) {
            return getDefaultRequiredExtensionNames();
        }

        final List<RequiresExtensionType> requiresExtensions = this.extensionInfo.getExtension().getRequiresExtension();

        if (isEmpty(requiresExtensions)) {
            return getDefaultRequiredExtensionNames();
        }

        final Set<String> requiredExtensionNames = new HashSet<>(requiresExtensions.size());

        requiredExtensionNames.addAll(requiresExtensions.stream()
            .map(RequiresExtensionType::getName)
            .toList());

        requiredExtensionNames.addAll(getAdditionalRequiredExtensionNames());

        if (this.hasHmcModule()) {
            requiredExtensionNames.add(HybrisConstants.EXTENSION_NAME_HMC);
        }

        if (this.hasBackofficeModule()) {
            requiredExtensionNames.add(HybrisConstants.EXTENSION_NAME_BACK_OFFICE);
        }

        return Collections.unmodifiableSet(requiredExtensionNames);
    }

    public boolean hasHmcModule() {
        return null != this.extensionInfo.getExtension().getHmcmodule();
    }

    public boolean isHacAddon() {
        return this.isMetaKeySetToTrue(EXTENSION_META_KEY_HAC_MODULE);
    }

    public boolean hasBackofficeModule() {
        return this.isMetaKeySetToTrue(EXTENSION_META_KEY_BACKOFFICE_MODULE) && this.doesBackofficeDirectoryExist();
    }

    protected boolean isMetaKeySetToTrue(@NotNull final String metaKeyName) {
        final var value = metas.get(metaKeyName);

        if (value == null) return false;

        return Boolean.TRUE.toString().equals(value);
    }

    protected boolean doesBackofficeDirectoryExist() {
        return new File(this.getRootDirectory(), BACKOFFICE_MODULE_DIRECTORY).isDirectory();
    }

    @NotNull
    @Override
    public List<JavaLibraryDescriptor> getLibraryDescriptors() {
        final List<JavaLibraryDescriptor> libs = new ArrayList<>();
        final HybrisModuleDescriptorType descriptorType = getDescriptorType();
        final boolean importOotbModulesInReadOnlyMode = getRootProjectDescriptor().isImportOotbModulesInReadOnlyMode();

        if (importOotbModulesInReadOnlyMode) {

            if (descriptorType != CUSTOM) {
                libs.add(new DefaultJavaLibraryDescriptor(
                    new File(this.getRootDirectory(), WEB_INF_CLASSES_DIRECTORY),
                    new File(this.getRootDirectory(), HybrisConstants.WEB_SRC_DIRECTORY),
                    false, true
                ));

                for (String srcDirName : HybrisConstants.SRC_DIR_NAMES) {
                    libs.add(new DefaultJavaLibraryDescriptor(
                        new File(this.getRootDirectory(), HybrisConstants.JAVA_COMPILER_OUTPUT_PATH),
                        new File(this.getRootDirectory(), srcDirName),
                        true, true
                    ));
                }

                libs.add(new DefaultJavaLibraryDescriptor(
                    new File(this.getRootDirectory(), HybrisConstants.RESOURCES_DIRECTORY),
                    true, true
                ));

                final File hmcModuleDirectory = new File(this.getRootDirectory(), HMC_MODULE_DIRECTORY);
                libs.add(new DefaultJavaLibraryDescriptor(
                    new File(hmcModuleDirectory, HybrisConstants.RESOURCES_DIRECTORY),
                    true, true
                ));
            }
        }

        // https://hybris-integration.atlassian.net/browse/IIP-355
        // HAC addons can not be compiled correctly by Intellij build because
        // "hybris/bin/platform/ext/hac/web/webroot/WEB-INF/classes" from "hac" extension is not registered
        // as a dependency for HAC addons.
        if (this.isHacAddon()) {
            libs.add(new DefaultJavaLibraryDescriptor(
                new File(this.getRootProjectDescriptor().getHybrisDistributionDirectory(), HAC_WEB_INF_CLASSES),
                false, true
            ));
        }

        final File webSrcDir = new File(this.getRootDirectory(), HybrisConstants.WEB_SRC_DIRECTORY);

        if (!webSrcDir.exists()) {
            if (!importOotbModulesInReadOnlyMode) {
                libs.add(new DefaultJavaLibraryDescriptor(
                    new File(this.getRootDirectory(), WEB_INF_CLASSES_DIRECTORY), false, true
                ));
            }
        }

        addServerJar(libs);

        libs.add(new DefaultJavaLibraryDescriptor(
            new File(this.getRootDirectory(), HybrisConstants.LIB_DIRECTORY),
            true,
            LibraryDescriptorType.LIB
        ));

        libs.add(new DefaultJavaLibraryDescriptor(
            new File(this.getRootDirectory(), HybrisConstants.WEB_WEBINF_LIB_DIRECTORY),
            false,
            LibraryDescriptorType.WEB_INF_LIB
        ));

        libs.add(new DefaultJavaLibraryDescriptor(
            new File(this.getRootDirectory(), HybrisConstants.COMMONWEB_WEBINF_LIB_DIRECTORY),
            true
        ));

        libs.add(new DefaultJavaLibraryDescriptor(
            new File(this.getRootDirectory(), HybrisConstants.HMC_LIB_DIRECTORY),
            true
        ));

        libs.add(new DefaultJavaLibraryDescriptor(
            new File(this.getRootDirectory(), HybrisConstants.BACKOFFICE_LIB_DIRECTORY),
            true
        ));

        if (this.hasHmcModule()) {
            getRootProjectDescriptor()
                .getModulesChosenForImport().stream()
                .filter(e -> e.getName().equals(HybrisConstants.EXTENSION_NAME_HMC))
                .findFirst()
                .ifPresent(hmcModule -> {
                    libs.add(new DefaultJavaLibraryDescriptor(
                        new File(
                            hmcModule.getRootDirectory(),
                            HybrisConstants.WEB_INF_CLASSES_DIRECTORY
                        ),
                        false, true
                    ));
                });
        }

        final Project currentProject = getRootProjectDescriptor().getProject();
        if (currentProject != null) {
            addLibsForBackofficeModule(libs, currentProject);
        }

        if (this.isAddOn()) {
            this.processAddOnBackwardDependencies(libs);
        }

        return Collections.unmodifiableList(libs);
    }

    private void addLibsForBackofficeModule(final List<JavaLibraryDescriptor> libs, final Project currentProject) {
        if (this.hasBackofficeModule()) {
            libs.add(new DefaultJavaLibraryDescriptor(
                new File(
                    this.getRootProjectDescriptor().getHybrisDistributionDirectory(),
                    HybrisProjectSettingsComponent.getInstance(currentProject).getBackofficeWebInfLib()
                ),
                false, false
            ));
            libs.add(new DefaultJavaLibraryDescriptor(
                new File(
                    this.getRootProjectDescriptor().getHybrisDistributionDirectory(),
                    HybrisProjectSettingsComponent.getInstance(currentProject).getBackofficeWebInfClasses()
                ),
                false, true
            ));
        }
    }

    private void addServerJar(final List<JavaLibraryDescriptor> libs) {
        final File binDir = new File(this.getRootDirectory(), HybrisConstants.BIN_DIRECTORY);
        if (!binDir.isDirectory()) {
            return;
        }
        final File[] serverJars = binDir.listFiles((dir, name) -> name.endsWith(HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX));
        if (serverJars == null || serverJars.length == 0) {
            return;
        }
        for (String srcDirName : HybrisConstants.SRC_DIR_NAMES) {
            final File srcDir = new File(this.getRootDirectory(), srcDirName);
            for (File serverJar : serverJars) {
                libs.add(new DefaultJavaLibraryDescriptor(serverJar, srcDir.isDirectory() ? srcDir : null, true, true));
            }
        }
    }

    protected void processAddOnBackwardDependencies(@NotNull final List<JavaLibraryDescriptor> libs) {
        Validate.notNull(libs);

        if (!getRootProjectDescriptor().isCreateBackwardCyclicDependenciesForAddOn()) {
            return;
        }

        final List<DefaultJavaLibraryDescriptor> backwardDependencies =
            this.getDependenciesTree()
                .stream()
                .filter(moduleDescriptor -> moduleDescriptor.getRequiredExtensionNames().contains(this.getName()))
                .map(moduleDescriptor -> new DefaultJavaLibraryDescriptor(
                    new File(moduleDescriptor.getRootDirectory(), HybrisConstants.WEB_WEBINF_LIB_DIRECTORY),
                    false,
                    false
                ))
                .toList();

        libs.addAll(backwardDependencies);
    }

    @Override
    public boolean isPreselected() {
        return isInLocalExtensions();
    }

    @Override
    public @NotNull ExtensionDescriptor getExtensionDescriptor() {
        return new ExtensionDescriptor(
            getName(),
            getDescriptorType(),
            isMetaKeySetToTrue(EXTENSION_META_KEY_BACKOFFICE_MODULE),
            isMetaKeySetToTrue(EXTENSION_META_KEY_HAC_MODULE),
            isMetaKeySetToTrue(EXTENSION_META_KEY_DEPRECATED),
            isMetaKeySetToTrue(EXTENSION_META_KEY_EXT_GEN),
            getRequiredExtensionNames().contains(HybrisConstants.EXTENSION_NAME_ADDONSUPPORT),
            metas.get(EXTENSION_META_KEY_CLASSPATHGEN),
            metas.get(EXTENSION_META_KEY_MODULE_GEN)
        );
    }

    protected Set<String> getDefaultRequiredExtensionNames() {
        return Collections.unmodifiableSet(Sets.newHashSet(HybrisConstants.EXTENSION_NAME_PLATFORM));
    }

    protected Collection<? extends String> getAdditionalRequiredExtensionNames() {
        return Collections.singleton(HybrisConstants.EXTENSION_NAME_PLATFORM);
    }
}
