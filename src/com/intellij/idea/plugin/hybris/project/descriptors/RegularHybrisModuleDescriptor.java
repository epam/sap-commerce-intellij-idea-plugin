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
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.ObjectFactory;
import com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo.RequiresExtensionType;
import com.intellij.openapi.diagnostic.Logger;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.BACKOFFICE_MODULE_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.EXTENSION_META_KEY_HAC_MODULE;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HAC_WEB_INF_CLASSES;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HMC_MODULE_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.WEB_INF_CLASSES_DIRECTORY;
import static com.intellij.idea.plugin.hybris.common.utils.CollectionUtils.emptyListIfNull;
import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType.CUSTOM;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created 3:55 PM 13 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public abstract class RegularHybrisModuleDescriptor extends AbstractHybrisModuleDescriptor {

    private static final Logger LOG = Logger.getInstance(RegularHybrisModuleDescriptor.class);

    @NotNull
    protected final String moduleName;
    @NotNull
    protected final ExtensionInfo extensionInfo;

    public RegularHybrisModuleDescriptor(
        @NotNull final File moduleRootDirectory,
        @NotNull final HybrisProjectDescriptor rootProjectDescriptor
    ) throws HybrisConfigurationException {
        super(moduleRootDirectory, rootProjectDescriptor);

        Validate.notNull(moduleRootDirectory);

        final File hybrisProjectFile = new File(moduleRootDirectory, HybrisConstants.EXTENSION_INFO_XML);

        final ExtensionInfo extensionInfo = this.unmarshalExtensionInfo(hybrisProjectFile);
        if (null == extensionInfo) {
            throw new HybrisConfigurationException("Can not unmarshal " + hybrisProjectFile);
        }

        this.extensionInfo = extensionInfo;

        if (null == extensionInfo.getExtension() || isBlank(extensionInfo.getExtension().getName())) {
            throw new HybrisConfigurationException("Can not find module name using path: " + moduleRootDirectory);
        }

        this.moduleName = extensionInfo.getExtension().getName();
    }

    @Nullable
    private ExtensionInfo unmarshalExtensionInfo(@NotNull final File file) {
        Validate.notNull(file);

        try {
            return (ExtensionInfo) JAXBContext.newInstance(
                "com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo",
                ObjectFactory.class.getClassLoader())
                                              .createUnmarshaller()
                                              .unmarshal(file);
        } catch (final JAXBException e) {
            LOG.error("Can not unmarshal " + file.getAbsolutePath(), e);
        }

        return null;
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

    @Override
    @NotNull
    public String getName() {
        return moduleName;
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

        final Set<String> requiredExtensionNames = new HashSet<String>(requiresExtensions.size());

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
        Validate.notEmpty(metaKeyName);

        return emptyListIfNull(this.extensionInfo.getExtension().getMeta()).stream().anyMatch(
            meta -> metaKeyName.equalsIgnoreCase(meta.getKey()) && Boolean.TRUE.toString().equals(meta.getValue())
        );
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

        if (this.hasBackofficeModule()) {

            libs.add(new DefaultJavaLibraryDescriptor(
                new File(
                    this.getRootProjectDescriptor().getHybrisDistributionDirectory(),
                    CommonIdeaService.getInstance().getBackofficeWebInfLib(getRootProjectDescriptor().getProject())
                ),
                false, false
            ));
            libs.add(new DefaultJavaLibraryDescriptor(
                new File(
                    this.getRootProjectDescriptor().getHybrisDistributionDirectory(),
                    CommonIdeaService.getInstance().getBackofficeWebInfClasses(getRootProjectDescriptor().getProject())
                ),
                false, true
            ));
        }

        if (this.isAddOn()) {
            this.processAddOnBackwardDependencies(libs);
        }

        return Collections.unmodifiableList(libs);
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


    protected Set<String> getDefaultRequiredExtensionNames() {
        return Collections.unmodifiableSet(Sets.newHashSet(HybrisConstants.EXTENSION_NAME_PLATFORM));
    }

    protected Collection<? extends String> getAdditionalRequiredExtensionNames() {
        return Collections.singleton(HybrisConstants.EXTENSION_NAME_PLATFORM);
    }
}
