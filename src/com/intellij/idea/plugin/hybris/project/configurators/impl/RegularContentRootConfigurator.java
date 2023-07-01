/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.project.configurators.ContentRootConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType;
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.*;

public class RegularContentRootConfigurator implements ContentRootConfigurator {

    // module name -> relative paths
    private static final Map<String, List<String>> ROOTS_TO_IGNORE = new HashMap<>();

    static {
        ROOTS_TO_IGNORE.put("acceleratorstorefrontcommons", Collections.singletonList("commonweb/testsrc"));
    }

    @Override
    public void configure(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final ModuleDescriptor moduleDescriptor
    ) {
        final ContentEntry contentEntry = modifiableRootModel.addContentEntry(VfsUtil.pathToUrl(
            moduleDescriptor.getModuleRootDirectory().getAbsolutePath()
        ));

        final List<File> dirsToIgnore = CollectionUtils.emptyIfNull(ROOTS_TO_IGNORE.get(moduleDescriptor.getName())).stream()
            .map(relPath -> new File(moduleDescriptor.getModuleRootDirectory(), relPath))
            .collect(Collectors.toList());

        configureCommonRoots(moduleDescriptor, contentEntry, dirsToIgnore);

        if (moduleDescriptor instanceof final CCv2ModuleDescriptor yCCv2ModuleDescriptor) {
            contentEntry.addExcludePattern("hybris");
        }
        if (moduleDescriptor instanceof final YWebSubModuleDescriptor ySubModuleDescriptor) {
            configureSubModule(ySubModuleDescriptor, contentEntry, dirsToIgnore);
        }
        if (moduleDescriptor instanceof final YCommonWebSubModuleDescriptor ySubModuleDescriptor) {
            configureSubModule(ySubModuleDescriptor, contentEntry);
        }
        if (moduleDescriptor instanceof final YAcceleratorAddonSubModuleDescriptor ySubModuleDescriptor) {
            configureSubModule(ySubModuleDescriptor, contentEntry);
        }
        if (moduleDescriptor instanceof final YBackofficeSubModuleDescriptor ySubModuleDescriptor) {
            configureSubModule(ySubModuleDescriptor, contentEntry, dirsToIgnore);
        }

        configurePlatformRoots(moduleDescriptor, contentEntry);
    }

    protected void configureSubModule(
        @NotNull final YWebSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        configureWebRoots(moduleDescriptor, contentEntry);
    }

    protected void configureCommonRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        for (String srcDirName : SRC_DIR_NAMES) {
            addSourceFolderIfNotIgnored(
                contentEntry,
                new File(moduleDescriptor.getModuleRootDirectory(), srcDirName),
                JavaSourceRootType.SOURCE,
                dirsToIgnore
            );
        }

        addSourceFolderIfNotIgnored(
            contentEntry,
            new File(moduleDescriptor.getModuleRootDirectory(), GEN_SRC_DIRECTORY),
            JavaSourceRootType.SOURCE,
            JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
            dirsToIgnore
        );

        if (moduleDescriptor instanceof YCustomRegularModuleDescriptor
            || !moduleDescriptor.getRootProjectDescriptor().isExcludeTestSources()) {
            addTestSourceRoots(contentEntry, moduleDescriptor.getModuleRootDirectory(), dirsToIgnore);
        } else {
            excludeTestSourceRoots(contentEntry, moduleDescriptor.getModuleRootDirectory());
        }

        configureResourceDirectory(contentEntry, moduleDescriptor, dirsToIgnore);

        excludeCommonNeedlessDirs(contentEntry, moduleDescriptor);
    }

    protected void configureResourceDirectory(
        @NotNull final ContentEntry contentEntry,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final List<File> dirsToIgnore
    ) {
        final File resourcesDirectory = new File(moduleDescriptor.getModuleRootDirectory(), RESOURCES_DIRECTORY);

        if (!isResourceDirExcluded(moduleDescriptor.getName())) {
            addSourceFolderIfNotIgnored(contentEntry, resourcesDirectory, JavaResourceRootType.RESOURCE, dirsToIgnore);
        } else {
            excludeDirectory(contentEntry, resourcesDirectory);
        }
    }

    protected void excludeCommonNeedlessDirs(
        final ContentEntry contentEntry,
        final ModuleDescriptor moduleDescriptor
    ) {
        excludeSubDirectories(contentEntry, moduleDescriptor.getModuleRootDirectory(), Arrays.asList(
            EXTERNAL_TOOL_BUILDERS_DIRECTORY,
            SETTINGS_DIRECTORY,
            TEST_CLASSES_DIRECTORY,
            ECLIPSE_BIN_DIRECTORY,
            NODE_MODULES_DIRECTORY,
            BOWER_COMPONENTS_DIRECTORY,
            JS_TARGET_DIRECTORY
        ));

        if (
            moduleDescriptor.getDescriptorType() == ModuleDescriptorType.CUSTOM ||
                !moduleDescriptor.getRootProjectDescriptor().isImportOotbModulesInReadOnlyMode()
        ) {
            excludeDirectory(contentEntry, new File(moduleDescriptor.getModuleRootDirectory(), CLASSES_DIRECTORY));
        }
    }

    private void excludeSubDirectories(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File dir,
        @NotNull final Iterable<String> names
    ) {
        for (String subDirName : names) {
            excludeDirectory(contentEntry, new File(dir, subDirName));
        }
    }

    private static void excludeDirectory(@NotNull final ContentEntry contentEntry, @NotNull final File dir) {
        contentEntry.addExcludeFolder(VfsUtil.pathToUrl(dir.getAbsolutePath()));
    }

    protected void configureWebRoots(
        @NotNull final YSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry
    ) {
        configureWebModuleRoots(moduleDescriptor, contentEntry);
    }

    protected void configureSubModule(
        @NotNull final YCommonWebSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry
    ) {
        configureWebModuleRoots(moduleDescriptor, contentEntry);
    }

    protected void configureSubModule(
        @NotNull final YAcceleratorAddonSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry
    ) {
        this.configureWebRoots(moduleDescriptor, contentEntry);
//        this.configureAdditionalRoots(moduleDescriptor, HMC_MODULE_DIRECTORY, contentEntry, commonWebModuleDirectory);
    }

    protected void configureSubModule(
        @NotNull final YBackofficeSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore
    ) {
        final File backOfficeModuleDirectory = moduleDescriptor.getModuleRootDirectory();

        if (!moduleDescriptor.getRootProjectDescriptor().isExcludeTestSources()) {
            addTestSourceRoots(contentEntry, backOfficeModuleDirectory, dirsToIgnore);
        } else {
            excludeTestSourceRoots(contentEntry, backOfficeModuleDirectory);
        }

        final File hmcResourcesDirectory = new File(backOfficeModuleDirectory, RESOURCES_DIRECTORY);
        contentEntry.addSourceFolder(
            VfsUtil.pathToUrl(hmcResourcesDirectory.getAbsolutePath()),
            JavaResourceRootType.RESOURCE
        );
        excludeDirectory(contentEntry, new File(backOfficeModuleDirectory, CLASSES_DIRECTORY));
    }

    protected void configurePlatformRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry
    ) {
        if (!HybrisConstants.EXTENSION_NAME_PLATFORM.equalsIgnoreCase(moduleDescriptor.getName())) {
            return;
        }
        final File rootDirectory = moduleDescriptor.getModuleRootDirectory();
        final File platformBootstrapDirectory = new File(rootDirectory, PLATFORM_BOOTSTRAP_DIRECTORY);

        if (!moduleDescriptor.getRootProjectDescriptor().isImportOotbModulesInReadOnlyMode()) {

            final File platformBootstrapResourcesDirectory = new File(platformBootstrapDirectory, RESOURCES_DIRECTORY);
            contentEntry.addSourceFolder(
                VfsUtil.pathToUrl(platformBootstrapResourcesDirectory.getAbsolutePath()),
                JavaResourceRootType.RESOURCE
            );
        }

        excludeDirectory(contentEntry, new File(platformBootstrapDirectory, PLATFORM_MODEL_CLASSES_DIRECTORY));

        File tomcat6 = new File(rootDirectory, PLATFORM_TOMCAT_6_DIRECTORY);
        if (tomcat6.exists()) {
            excludeDirectory(contentEntry, tomcat6);
        } else {
            excludeDirectory(contentEntry, new File(rootDirectory, PLATFORM_TOMCAT_DIRECTORY));
        }
        contentEntry.addExcludePattern("apache-ant-*");
    }

    protected void configureWebModuleRoots(
        @NotNull final YSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry
    ) {
        excludeSubDirectories(
            contentEntry,
            moduleDescriptor.getModuleRootDirectory(),
            Arrays.asList(ADDON_SRC_DIRECTORY, TEST_CLASSES_DIRECTORY, COMMON_WEB_SRC_DIRECTORY)
        );
        configureWebInf(contentEntry, moduleDescriptor);
    }

    private static void addTestSourceRoots(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File dir,
        @NotNull final List<File> dirsToIgnore
    ) {

        for (String testSrcDirName : TEST_SRC_DIR_NAMES) {
            addSourceFolderIfNotIgnored(
                contentEntry,
                new File(dir, testSrcDirName),
                JavaSourceRootType.TEST_SOURCE,
                dirsToIgnore
            );
        }
    }

    private static void excludeTestSourceRoots(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File dir
    ) {

        for (String testSrcDirName : TEST_SRC_DIR_NAMES) {
            excludeDirectory(contentEntry, new File(dir, testSrcDirName));
        }
    }

    protected static <P extends JpsElement> void addSourceFolderIfNotIgnored(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File testSrcDir,
        @NotNull final JpsModuleSourceRootType<P> rootType,
        @NotNull final List<File> dirsToIgnore
    ) {
        addSourceFolderIfNotIgnored(
            contentEntry,
            testSrcDir,
            rootType,
            rootType.createDefaultProperties(),
            dirsToIgnore
        );
    }

    protected boolean isResourceDirExcluded(final String moduleName) {
        List<String> extensionsResourcesToExcludeList = HybrisApplicationSettingsComponent.getInstance()
            .getState()
            .getExtensionsResourcesToExclude();
        return (CollectionUtils.isNotEmpty(extensionsResourcesToExcludeList) && extensionsResourcesToExcludeList
            .contains(moduleName));
    }

    // /Users/Evgenii/work/upwork/test-projects/pawel-hybris/bin/ext-accelerator/acceleratorstorefrontcommons/testsrc
    // /Users/Evgenii/work/upwork/test-projects/pawel-hybris/bin/ext-accelerator/acceleratorstorefrontcommons/commonweb/testsrc

    private static <P extends JpsElement> void addSourceFolderIfNotIgnored(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File testSrcDir,
        @NotNull final JpsModuleSourceRootType<P> rootType,
        @NotNull P properties,
        @NotNull final List<File> dirsToIgnore
    ) {
        if (dirsToIgnore.stream().noneMatch(it -> FileUtil.isAncestor(it, testSrcDir, false))) {
            contentEntry.addSourceFolder(
                VfsUtil.pathToUrl(testSrcDir.getAbsolutePath()),
                rootType,
                properties
            );
        }
    }

    protected void configureWebInf(
        final ContentEntry contentEntry,
        final YSubModuleDescriptor moduleDescriptor
    ) {
        final File rootDirectory = moduleDescriptor.getModuleRootDirectory();

        if (moduleDescriptor.getDescriptorType() == ModuleDescriptorType.CUSTOM
            || (!moduleDescriptor.getRootProjectDescriptor().isImportOotbModulesInReadOnlyMode()
                && srcDirectoriesExists(rootDirectory))
        ) {
            excludeDirectory(contentEntry, new File(rootDirectory, WEBROOT_WEBINF_CLASSES_PATH));
        }
    }

    private boolean srcDirectoriesExists(final File webModuleDirectory) {
        return TEST_SRC_DIR_NAMES.stream().anyMatch(s -> new File(webModuleDirectory, s).exists());
    }
}
