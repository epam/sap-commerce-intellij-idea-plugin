/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.*;
import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;

public class DefaultContentRootConfigurator implements ContentRootConfigurator {

    // module name -> relative paths
    private static final Map<String, List<String>> ROOTS_TO_IGNORE = new HashMap<>();

    static {
        ROOTS_TO_IGNORE.put("acceleratorstorefrontcommons", Collections.singletonList("commonweb/testsrc"));
    }

    @Override
    public void configure(
        @NotNull final ProgressIndicator indicator,
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final HybrisApplicationSettings appSettings
    ) {
        indicator.setText2(message("hybris.project.import.module.content"));
        final var contentEntry = modifiableRootModel.addContentEntry(VfsUtil.pathToUrl(
            moduleDescriptor.getModuleRootDirectory().getAbsolutePath()
        ));

        final var dirsToIgnore = CollectionUtils.emptyIfNull(ROOTS_TO_IGNORE.get(moduleDescriptor.getName())).stream()
            .map(relPath -> new File(moduleDescriptor.getModuleRootDirectory(), relPath))
            .collect(Collectors.toList());

        configureCommonRoots(moduleDescriptor, contentEntry, dirsToIgnore, appSettings);

        if (moduleDescriptor instanceof final CCv2ModuleDescriptor yCCv2ModuleDescriptor) {
            contentEntry.addExcludePattern(HybrisConstants.HYBRIS_DIRECTORY);
        }
        if (moduleDescriptor instanceof final YWebSubModuleDescriptor ySubModuleDescriptor) {
            configureWebRoots(ySubModuleDescriptor, contentEntry, appSettings);
        }
        if (moduleDescriptor instanceof final YCommonWebSubModuleDescriptor ySubModuleDescriptor) {
            configureWebModuleRoots(ySubModuleDescriptor, contentEntry);
        }
        if (moduleDescriptor instanceof final YAcceleratorAddonSubModuleDescriptor ySubModuleDescriptor) {
            configureWebModuleRoots(ySubModuleDescriptor, contentEntry);
        }
        if (moduleDescriptor instanceof final PlatformModuleDescriptor platformModuleDescriptor) {
            configurePlatformRoots(platformModuleDescriptor, contentEntry, dirsToIgnore, appSettings);
        }
    }

    protected void configureCommonRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final HybrisApplicationSettings appSettings
    ) {
        final var rootProjectDescriptor = moduleDescriptor.getRootProjectDescriptor();
        final var customModuleDescriptor = isCustomModuleDescriptor(moduleDescriptor);
        if (customModuleDescriptor
            || !rootProjectDescriptor.isImportOotbModulesInReadOnlyMode()
            || HybrisConstants.EXTENSION_NAME_PLATFORM_SERVICES.equals(moduleDescriptor.getName())
        ) {
            addSourceRoots(contentEntry, moduleDescriptor.getModuleRootDirectory(), dirsToIgnore, appSettings, SRC_DIR_NAMES, JavaSourceRootType.SOURCE);

            if (customModuleDescriptor || !rootProjectDescriptor.isExcludeTestSources()) {
                addSourceRoots(contentEntry, moduleDescriptor.getModuleRootDirectory(), dirsToIgnore, appSettings, TEST_SRC_DIR_NAMES, JavaSourceRootType.TEST_SOURCE);
            }

            addSourceFolderIfNotIgnored(
                contentEntry,
                new File(moduleDescriptor.getModuleRootDirectory(), GEN_SRC_DIRECTORY),
                JavaSourceRootType.SOURCE,
                JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
                dirsToIgnore, appSettings
            );
        }

        configureResourceDirectory(contentEntry, moduleDescriptor, dirsToIgnore, appSettings);

        excludeCommonNeedlessDirs(contentEntry, moduleDescriptor);
    }

    protected void configureResourceDirectory(
        @NotNull final ContentEntry contentEntry,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final HybrisApplicationSettings appSettings
    ) {
        final var resourcesDirectory = new File(moduleDescriptor.getModuleRootDirectory(), RESOURCES_DIRECTORY);

        addSourceFolderIfNotIgnored(contentEntry, resourcesDirectory, JavaResourceRootType.RESOURCE, dirsToIgnore, appSettings);

        final var extensionsResourcesToExcludeList = appSettings.getExtensionsResourcesToExclude();
        final var shouldExcludeResourcesDir = CollectionUtils.isNotEmpty(extensionsResourcesToExcludeList)
            && extensionsResourcesToExcludeList.contains(moduleDescriptor.getName());

        if (shouldExcludeResourcesDir) {
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

        if (isCustomModuleDescriptor(moduleDescriptor)
            || !moduleDescriptor.getRootProjectDescriptor().isImportOotbModulesInReadOnlyMode()) {
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
        @NotNull final YWebSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final HybrisApplicationSettings appSettings
    ) {
        configureWebModuleRoots(moduleDescriptor, contentEntry);

        final var rootProjectDescriptor = moduleDescriptor.getRootProjectDescriptor();

        if (isCustomModuleDescriptor(moduleDescriptor) || !rootProjectDescriptor.isImportOotbModulesInReadOnlyMode()) {
            configureExternalModuleRoot(moduleDescriptor, contentEntry, appSettings, COMMON_WEB_SRC_DIRECTORY, JavaSourceRootType.SOURCE);
            configureExternalModuleRoot(moduleDescriptor, contentEntry, appSettings, ADDON_SRC_DIRECTORY, JavaSourceRootType.SOURCE);
        }
    }

    private static void configureExternalModuleRoot(
        final @NotNull YWebSubModuleDescriptor moduleDescriptor,
        final @NotNull ContentEntry contentEntry,
        final @NotNull HybrisApplicationSettings appSettings,
        final String sourceRoot,
        final JavaSourceRootType type
    ) {
        final var commonWebSrcDir = new File(moduleDescriptor.getModuleRootDirectory(), sourceRoot);

        if (!commonWebSrcDir.isDirectory()) return;

        final var additionalSources = commonWebSrcDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

        if (additionalSources == null || additionalSources.length == 0) return;

        final var directories = Arrays.stream(additionalSources)
            .map(File::getName)
            .toList();

        directories.stream()
            .map(it -> new File(commonWebSrcDir, it))
            .forEach(directory -> {
                addSourceFolderIfNotIgnored(
                    contentEntry,
                    directory,
                    type,
                    JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
                    Collections.emptyList(), appSettings
                );
            });
    }

    protected void configurePlatformRoots(
        @NotNull final PlatformModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        final List<File> dirsToIgnore,
        final HybrisApplicationSettings appSettings
    ) {
        final var rootDirectory = moduleDescriptor.getModuleRootDirectory();
        final var platformBootstrapDirectory = new File(rootDirectory, PLATFORM_BOOTSTRAP_DIRECTORY);

        addResourcesDirectory(contentEntry, platformBootstrapDirectory);
        // Only when bootstrap gensrc registered as source folder we can properly build the Class Hierarchy
        final var gensrcDirectory = new File(platformBootstrapDirectory, GEN_SRC_DIRECTORY);
        addSourceFolderIfNotIgnored(
            contentEntry,
            gensrcDirectory,
            JavaSourceRootType.SOURCE,
            JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
            dirsToIgnore, appSettings
        );

        excludeDirectory(contentEntry, gensrcDirectory);
        excludeDirectory(contentEntry, new File(platformBootstrapDirectory, PLATFORM_MODEL_CLASSES_DIRECTORY));

        final var tomcat6 = new File(rootDirectory, PLATFORM_TOMCAT_6_DIRECTORY);
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
            List.of(TEST_CLASSES_DIRECTORY)
        );
        configureWebInf(contentEntry, moduleDescriptor);
    }

    private static void addSourceRoots(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File dir,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final HybrisApplicationSettings appSettings,
        final List<String> directories,
        final JavaSourceRootType scope
    ) {
        for (final var directory : directories) {
            addSourceFolderIfNotIgnored(
                contentEntry,
                new File(dir, directory),
                scope,
                dirsToIgnore, appSettings
            );
        }
    }

    protected static <P extends JpsElement> void addSourceFolderIfNotIgnored(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File testSrcDir,
        @NotNull final JpsModuleSourceRootType<P> rootType,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final HybrisApplicationSettings appSettings
    ) {
        addSourceFolderIfNotIgnored(
            contentEntry,
            testSrcDir,
            rootType,
            rootType.createDefaultProperties(),
            dirsToIgnore, appSettings
        );
    }

    // /Users/Evgenii/work/upwork/test-projects/pawel-hybris/bin/ext-accelerator/acceleratorstorefrontcommons/testsrc
    // /Users/Evgenii/work/upwork/test-projects/pawel-hybris/bin/ext-accelerator/acceleratorstorefrontcommons/commonweb/testsrc

    private static <P extends JpsElement> void addSourceFolderIfNotIgnored(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File srcDir,
        @NotNull final JpsModuleSourceRootType<P> rootType,
        @NotNull P properties,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final HybrisApplicationSettings applicationSettings
    ) {
        if (dirsToIgnore.stream().noneMatch(it -> FileUtil.isAncestor(it, srcDir, false))) {
            final boolean ignoreEmpty = applicationSettings.getIgnoreNonExistingSourceDirectories();
            if (BooleanUtils.isTrue(ignoreEmpty) && !srcDir.exists()) {
                return;
            }
            contentEntry.addSourceFolder(
                VfsUtil.pathToUrl(srcDir.getAbsolutePath()),
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

        if (isCustomModuleDescriptor(moduleDescriptor)
            || (!moduleDescriptor.getRootProjectDescriptor().isImportOotbModulesInReadOnlyMode() && testSrcDirectoriesExists(rootDirectory))
        ) {
            excludeDirectory(contentEntry, new File(rootDirectory, WEBROOT_WEBINF_CLASSES_PATH));
        }
    }

    private static boolean isCustomModuleDescriptor(final @NotNull ModuleDescriptor moduleDescriptor) {
        return moduleDescriptor instanceof YCustomRegularModuleDescriptor
            || (moduleDescriptor instanceof final YSubModuleDescriptor ySubModuleDescriptor && ySubModuleDescriptor.getOwner() instanceof YCustomRegularModuleDescriptor);
    }

    private void addResourcesDirectory(final @NotNull ContentEntry contentEntry, final File platformBootstrapDirectory) {
        final var platformBootstrapResourcesDirectory = new File(platformBootstrapDirectory, RESOURCES_DIRECTORY);
        contentEntry.addSourceFolder(
            VfsUtil.pathToUrl(platformBootstrapResourcesDirectory.getAbsolutePath()),
            JavaResourceRootType.RESOURCE
        );
    }

    private boolean testSrcDirectoriesExists(final File webModuleDirectory) {
        return TEST_SRC_DIR_NAMES.stream().anyMatch(s -> new File(webModuleDirectory, s).exists());
    }
}
