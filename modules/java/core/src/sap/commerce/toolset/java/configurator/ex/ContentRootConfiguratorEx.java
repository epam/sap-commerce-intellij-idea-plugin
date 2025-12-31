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

package sap.commerce.toolset.java.configurator.ex;

import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.extensioninfo.EiConstants;
import sap.commerce.toolset.project.ProjectConstants;
import sap.commerce.toolset.project.context.ProjectImportContext;
import sap.commerce.toolset.project.descriptor.ModuleDescriptor;
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor;
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor;
import sap.commerce.toolset.project.descriptor.impl.*;
import sap.commerce.toolset.settings.ApplicationSettings;
import sap.commerce.toolset.util.FileUtilKt;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Deprecated(since = "Migrate to kotlin and merge with the configurator")
public final class ContentRootConfiguratorEx {

    // module name -> relative paths
    private static final Map<String, List<String>> ROOTS_TO_IGNORE = new HashMap<>();

    static {
        ROOTS_TO_IGNORE.put("acceleratorstorefrontcommons", Collections.singletonList("commonweb/testsrc"));
    }

    private ContentRootConfiguratorEx() {
    }

    public static void configure(
        final @NotNull ProjectImportContext importContext,
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final ModuleDescriptor moduleDescriptor
    ) {
        final var applicationSettings = ApplicationSettings.getInstance();
        final var moduleRootDirectory = moduleDescriptor.getModuleRootDirectory();
        final var vf = VfsUtil.findFile(moduleRootDirectory, true);

        if (vf == null) return;

        final var contentEntry = modifiableRootModel.addContentEntry(vf);

        final var dirsToIgnore = ROOTS_TO_IGNORE.getOrDefault(moduleDescriptor.getName(), List.of()).stream()
            .map(moduleRootDirectory::resolve)
            .collect(Collectors.toList());

        configureCommonRoots(importContext, moduleDescriptor, contentEntry, dirsToIgnore, applicationSettings);

        switch (moduleDescriptor) {
            case final YWebSubModuleDescriptor descriptor -> configureWebRoots(importContext, descriptor, contentEntry, applicationSettings);
            case final YCommonWebSubModuleDescriptor descriptor -> configureWebModuleRoots(importContext, descriptor, contentEntry);
            case final YAcceleratorAddonSubModuleDescriptor descriptor -> configureWebModuleRoots(importContext, descriptor, contentEntry);
            case final PlatformModuleDescriptor descriptor -> configurePlatformRoots(descriptor, contentEntry, dirsToIgnore, applicationSettings);

            default -> {
            }
        }
    }

    private static void configureCommonRoots(
        final @NotNull ProjectImportContext importContext, @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<Path> dirsToIgnore,
        @NotNull final ApplicationSettings applicationSettings
    ) {
        final var customModuleDescriptor = isCustomModuleDescriptor(moduleDescriptor);
        if (customModuleDescriptor
            || !importContext.getSettings().getImportOOTBModulesInReadOnlyMode()
            || EiConstants.Extension.PLATFORM_SERVICES.equals(moduleDescriptor.getName())
        ) {
            final var moduleRootDirectory = moduleDescriptor.getModuleRootDirectory();

            addSourceRoots(contentEntry, moduleRootDirectory, dirsToIgnore, applicationSettings, ProjectConstants.Directory.SRC_DIR_NAMES, JavaSourceRootType.SOURCE);

            if (customModuleDescriptor || !importContext.getSettings().getExcludeTestSources()) {
                addSourceRoots(contentEntry, moduleRootDirectory, dirsToIgnore, applicationSettings, ProjectConstants.Directory.TEST_SRC_DIR_NAMES, JavaSourceRootType.TEST_SOURCE);
            }

            addSourceFolderIfNotIgnored(
                contentEntry,
                moduleRootDirectory.resolve(ProjectConstants.Directory.GEN_SRC),
                JavaSourceRootType.SOURCE,
                JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
                dirsToIgnore, applicationSettings
            );

            configureResourceDirectory(contentEntry, moduleDescriptor, dirsToIgnore, applicationSettings);
        }

        excludeCommonNeedlessDirs(importContext, contentEntry, moduleDescriptor);
    }

    private static void configureResourceDirectory(
        @NotNull final ContentEntry contentEntry,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final List<Path> dirsToIgnore,
        @NotNull final ApplicationSettings applicationSettings
    ) {
        final var resourcesDirectory = moduleDescriptor.getModuleRootDirectory().resolve(ProjectConstants.Directory.RESOURCES);

        final var rootType = JavaResourceRootType.RESOURCE;
        final var properties = moduleDescriptor instanceof YBackofficeSubModuleDescriptor
            ? JpsJavaExtensionService.getInstance().createResourceRootProperties("cockpitng", false)
            : rootType.createDefaultProperties();

        addSourceFolderIfNotIgnored(contentEntry, resourcesDirectory, rootType, properties, dirsToIgnore, applicationSettings);

        final var extensionsResourcesToExcludeList = applicationSettings.getExtensionsResourcesToExclude();
        final var shouldExcludeResourcesDir = CollectionUtils.isNotEmpty(extensionsResourcesToExcludeList)
            && extensionsResourcesToExcludeList.contains(moduleDescriptor.getName());

        if (shouldExcludeResourcesDir) {
            excludeDirectory(contentEntry, resourcesDirectory);
        }
    }

    private static void excludeCommonNeedlessDirs(
        final @NotNull ProjectImportContext importContext, final ContentEntry contentEntry,
        final ModuleDescriptor moduleDescriptor
    ) {
        excludeSubDirectories(contentEntry, moduleDescriptor.getModuleRootDirectory(), List.of(
            ProjectConstants.Directory.NODE_MODULES,
            HybrisConstants.EXTERNAL_TOOL_BUILDERS_DIRECTORY,
            HybrisConstants.SETTINGS_DIRECTORY,
            ProjectConstants.Directory.TEST_CLASSES,
            ProjectConstants.Directory.ECLIPSE_BIN,
            ProjectConstants.Directory.BOWER_COMPONENTS,
            ProjectConstants.Directory.JS_TARGET,
            HybrisConstants.SPOCK_META_INF_SERVICES_DIRECTORY
        ));

        if (isCustomModuleDescriptor(moduleDescriptor)
            || !importContext.getSettings().getImportOOTBModulesInReadOnlyMode()) {
            excludeDirectory(contentEntry, moduleDescriptor.getModuleRootDirectory().resolve(ProjectConstants.Directory.CLASSES));
        }
    }

    private static void excludeSubDirectories(
        @NotNull final ContentEntry contentEntry,
        @NotNull final Path dir,
        @NotNull final Iterable<String> names
    ) {
        for (final var subDirName : names) {
            excludeDirectory(contentEntry, dir.resolve(subDirName));
        }
    }

    private static void excludeDirectory(@NotNull final ContentEntry contentEntry, @NotNull final Path dir) {
        final var vf = VfsUtil.findFile(dir, true);
        if (vf != null) contentEntry.addExcludeFolder(vf);
    }

    private static void configureWebRoots(
        final @NotNull ProjectImportContext importContext, @NotNull final YWebSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final ApplicationSettings applicationSettings
    ) {
        configureWebModuleRoots(importContext, moduleDescriptor, contentEntry);

        if (isCustomModuleDescriptor(moduleDescriptor) || !importContext.getSettings().getImportOOTBModulesInReadOnlyMode()) {
            configureExternalModuleRoot(moduleDescriptor, contentEntry, applicationSettings, ProjectConstants.Directory.COMMON_WEB_SRC, JavaSourceRootType.SOURCE);
            configureExternalModuleRoot(moduleDescriptor, contentEntry, applicationSettings, ProjectConstants.Directory.ADDON_SRC, JavaSourceRootType.SOURCE);
        }
    }

    private static void configureExternalModuleRoot(
        final @NotNull YWebSubModuleDescriptor moduleDescriptor,
        final @NotNull ContentEntry contentEntry,
        final @NotNull ApplicationSettings applicationSettings,
        final String sourceRoot,
        final JavaSourceRootType type
    ) {
        final var commonWebSrcDir = moduleDescriptor.getModuleRootDirectory().resolve(sourceRoot);

        if (!FileUtilKt.getDirectoryExists(commonWebSrcDir)) return;

        final var additionalSources = commonWebSrcDir.toFile()
            .listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

        if (additionalSources == null || additionalSources.length == 0) return;

        final var directories = Arrays.stream(additionalSources)
            .map(File::getName)
            .toList();

        directories.stream()
            .map(commonWebSrcDir::resolve)
            .forEach(directory -> {
                addSourceFolderIfNotIgnored(
                    contentEntry,
                    directory,
                    type,
                    JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
                    Collections.emptyList(), applicationSettings
                );
            });
    }

    private static void configurePlatformRoots(
        @NotNull final PlatformModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        final List<Path> dirsToIgnore,
        final ApplicationSettings applicationSettings
    ) {
        final var rootDirectory = moduleDescriptor.getModuleRootDirectory();
        final var platformBootstrapDirectory = rootDirectory.resolve(ProjectConstants.Directory.BOOTSTRAP);

        addResourcesDirectory(contentEntry, platformBootstrapDirectory);
        // Only when bootstrap gensrc registered as source folder we can properly build the Class Hierarchy
        final var gensrcDirectory = platformBootstrapDirectory.resolve(ProjectConstants.Directory.GEN_SRC);
        addSourceFolderIfNotIgnored(
            contentEntry,
            gensrcDirectory,
            JavaSourceRootType.SOURCE,
            JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
            dirsToIgnore, applicationSettings
        );

        excludeDirectory(contentEntry, gensrcDirectory);
        excludeDirectory(contentEntry, platformBootstrapDirectory.resolve(ProjectConstants.Directory.MODEL_CLASSES));

        final var tomcat6 = rootDirectory.resolve(ProjectConstants.Directory.TOMCAT_6);
        if (FileUtilKt.getDirectoryExists(tomcat6)) {
            excludeDirectory(contentEntry, tomcat6);
        } else {
            excludeDirectory(contentEntry, rootDirectory.resolve(ProjectConstants.Directory.TOMCAT));
        }
        contentEntry.addExcludePattern("apache-ant-*");
    }

    private static void configureWebModuleRoots(
        final @NotNull ProjectImportContext importContext, @NotNull final YSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry
    ) {
        excludeSubDirectories(
            contentEntry,
            moduleDescriptor.getModuleRootDirectory(),
            List.of(ProjectConstants.Directory.TEST_CLASSES)
        );
        configureWebInf(importContext, contentEntry, moduleDescriptor);
    }

    private static void addSourceRoots(
        @NotNull final ContentEntry contentEntry,
        @NotNull final Path dir,
        @NotNull final List<Path> dirsToIgnore,
        @NotNull final ApplicationSettings applicationSettings,
        final List<String> directories,
        final JavaSourceRootType scope
    ) {
        for (final var directory : directories) {
            addSourceFolderIfNotIgnored(
                contentEntry,
                dir.resolve(directory),
                scope,
                scope.createDefaultProperties(),
                dirsToIgnore, applicationSettings
            );
        }
    }

    // /Users/Evgenii/work/upwork/test-projects/pawel-hybris/bin/ext-accelerator/acceleratorstorefrontcommons/testsrc
    // /Users/Evgenii/work/upwork/test-projects/pawel-hybris/bin/ext-accelerator/acceleratorstorefrontcommons/commonweb/testsrc

    private static <P extends JpsElement> void addSourceFolderIfNotIgnored(
        @NotNull final ContentEntry contentEntry,
        @NotNull final Path srcDir,
        @NotNull final JpsModuleSourceRootType<P> rootType,
        @NotNull final P properties,
        @NotNull final List<Path> dirsToIgnore,
        @NotNull final ApplicationSettings applicationSettings
    ) {
        if (dirsToIgnore.stream().noneMatch(it -> FileUtil.isAncestor(it.toFile(), srcDir.toFile(), false))) {
            final boolean ignoreEmpty = applicationSettings.getIgnoreNonExistingSourceDirectories();
            if (BooleanUtils.isTrue(ignoreEmpty) && !FileUtilKt.getDirectoryExists(srcDir)) {
                return;
            }

            final var vf = VfsUtil.findFile(srcDir, true);
            if (vf != null) contentEntry.addSourceFolder(
                vf,
                rootType,
                properties
            );
        }
    }

    private static void configureWebInf(
        final @NotNull ProjectImportContext importContext,
        final ContentEntry contentEntry,
        final YSubModuleDescriptor moduleDescriptor
    ) {
        final var rootDirectory = moduleDescriptor.getModuleRootDirectory();

        if (isCustomModuleDescriptor(moduleDescriptor)
            || (!importContext.getSettings().getImportOOTBModulesInReadOnlyMode() && testSrcDirectoriesExists(rootDirectory))
        ) {
            excludeDirectory(contentEntry, rootDirectory.resolve(ProjectConstants.Paths.INSTANCE.getACCELERATOR_ADDON_WEB()));
        }
    }

    private static boolean isCustomModuleDescriptor(final @NotNull ModuleDescriptor moduleDescriptor) {
        return moduleDescriptor instanceof YCustomRegularModuleDescriptor
            || (moduleDescriptor instanceof final YSubModuleDescriptor ySubModuleDescriptor && ySubModuleDescriptor.getOwner() instanceof YCustomRegularModuleDescriptor);
    }

    private static void addResourcesDirectory(final @NotNull ContentEntry contentEntry, final Path platformBootstrapDirectory) {
        final var platformBootstrapResourcesDirectory = platformBootstrapDirectory.resolve(ProjectConstants.Directory.RESOURCES);
        final var vf = VfsUtil.findFile(platformBootstrapResourcesDirectory, true);

        if (vf != null) contentEntry.addSourceFolder(vf, JavaResourceRootType.RESOURCE);
    }

    private static boolean testSrcDirectoriesExists(final Path webModuleDirectory) {
        return ProjectConstants.Directory.TEST_SRC_DIR_NAMES.stream()
            .anyMatch(s -> FileUtilKt.getDirectoryExists(webModuleDirectory.resolve(s)));
    }
}
