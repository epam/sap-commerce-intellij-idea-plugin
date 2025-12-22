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
import sap.commerce.toolset.project.ProjectConstants;
import sap.commerce.toolset.project.descriptor.ModuleDescriptor;
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor;
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor;
import sap.commerce.toolset.project.descriptor.impl.*;
import sap.commerce.toolset.settings.ApplicationSettings;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.stream.Collectors;

import static sap.commerce.toolset.HybrisConstants.WEBROOT_WEBINF_CLASSES_PATH;

public final class ContentRootConfiguratorEx {

    // module name -> relative paths
    private static final Map<String, List<String>> ROOTS_TO_IGNORE = new HashMap<>();

    static {
        ROOTS_TO_IGNORE.put("acceleratorstorefrontcommons", Collections.singletonList("commonweb/testsrc"));
    }

    private ContentRootConfiguratorEx() {
    }

    public static void configure(
        @NotNull final ModifiableRootModel modifiableRootModel,
        @NotNull final ModuleDescriptor moduleDescriptor
    ) {
        final var appSettings = ApplicationSettings.getInstance();

        final var contentEntry = modifiableRootModel.addContentEntry(VfsUtil.pathToUrl(
            moduleDescriptor.getModuleRootDirectory().getAbsolutePath()
        ));

        final var dirsToIgnore = ROOTS_TO_IGNORE.getOrDefault(moduleDescriptor.getName(), List.of()).stream()
            .map(relPath -> new File(moduleDescriptor.getModuleRootDirectory(), relPath))
            .collect(Collectors.toList());

        configureCommonRoots(moduleDescriptor, contentEntry, dirsToIgnore, appSettings);

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

    private static void configureCommonRoots(
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final ApplicationSettings appSettings
    ) {
        final var rootProjectDescriptor = moduleDescriptor.getRootProjectDescriptor();
        final var customModuleDescriptor = isCustomModuleDescriptor(moduleDescriptor);
        if (customModuleDescriptor
            || !rootProjectDescriptor.getImportContext().getImportOOTBModulesInReadOnlyMode()
            || ProjectConstants.Extension.PLATFORM_SERVICES.equals(moduleDescriptor.getName())
        ) {
            final var moduleRootDirectory = moduleDescriptor.getModuleRootDirectory();

            addSourceRoots(contentEntry, moduleRootDirectory, dirsToIgnore, appSettings, ProjectConstants.Directory.SRC_DIR_NAMES, JavaSourceRootType.SOURCE);

            if (customModuleDescriptor || !rootProjectDescriptor.getImportContext().getExcludeTestSources()) {
                addSourceRoots(contentEntry, moduleRootDirectory, dirsToIgnore, appSettings, ProjectConstants.Directory.TEST_SRC_DIR_NAMES, JavaSourceRootType.TEST_SOURCE);
            }

            addSourceFolderIfNotIgnored(
                contentEntry,
                new File(moduleRootDirectory, ProjectConstants.Directory.GEN_SRC),
                JavaSourceRootType.SOURCE,
                JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
                dirsToIgnore, appSettings
            );

            configureResourceDirectory(contentEntry, moduleDescriptor, dirsToIgnore, appSettings);
        }

        excludeCommonNeedlessDirs(contentEntry, moduleDescriptor);
    }

    private static void configureResourceDirectory(
        @NotNull final ContentEntry contentEntry,
        @NotNull final ModuleDescriptor moduleDescriptor,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final ApplicationSettings appSettings
    ) {
        final var resourcesDirectory = new File(moduleDescriptor.getModuleRootDirectory(), ProjectConstants.Directory.RESOURCES);

        final var rootType = JavaResourceRootType.RESOURCE;
        final var properties = moduleDescriptor instanceof YBackofficeSubModuleDescriptor
            ? JpsJavaExtensionService.getInstance().createResourceRootProperties("cockpitng", false)
            : rootType.createDefaultProperties();

        addSourceFolderIfNotIgnored(contentEntry, resourcesDirectory, rootType, properties, dirsToIgnore, appSettings);

        final var extensionsResourcesToExcludeList = appSettings.getExtensionsResourcesToExclude();
        final var shouldExcludeResourcesDir = CollectionUtils.isNotEmpty(extensionsResourcesToExcludeList)
            && extensionsResourcesToExcludeList.contains(moduleDescriptor.getName());

        if (shouldExcludeResourcesDir) {
            excludeDirectory(contentEntry, resourcesDirectory);
        }
    }

    private static void excludeCommonNeedlessDirs(
        final ContentEntry contentEntry,
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
            || !moduleDescriptor.getRootProjectDescriptor().getImportContext().getImportOOTBModulesInReadOnlyMode()) {
            excludeDirectory(contentEntry, new File(moduleDescriptor.getModuleRootDirectory(), ProjectConstants.Directory.CLASSES));
        }
    }

    private static void excludeSubDirectories(
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

    private static void configureWebRoots(
        @NotNull final YWebSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        @NotNull final ApplicationSettings appSettings
    ) {
        configureWebModuleRoots(moduleDescriptor, contentEntry);

        final var rootProjectDescriptor = moduleDescriptor.getRootProjectDescriptor();

        if (isCustomModuleDescriptor(moduleDescriptor) || !rootProjectDescriptor.getImportContext().getImportOOTBModulesInReadOnlyMode()) {
            configureExternalModuleRoot(moduleDescriptor, contentEntry, appSettings, ProjectConstants.Directory.COMMON_WEB_SRC, JavaSourceRootType.SOURCE);
            configureExternalModuleRoot(moduleDescriptor, contentEntry, appSettings, ProjectConstants.Directory.ADDON_SRC, JavaSourceRootType.SOURCE);
        }
    }

    private static void configureExternalModuleRoot(
        final @NotNull YWebSubModuleDescriptor moduleDescriptor,
        final @NotNull ContentEntry contentEntry,
        final @NotNull ApplicationSettings appSettings,
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

    private static void configurePlatformRoots(
        @NotNull final PlatformModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry,
        final List<File> dirsToIgnore,
        final ApplicationSettings appSettings
    ) {
        final var rootDirectory = moduleDescriptor.getModuleRootDirectory();
        final var platformBootstrapDirectory = new File(rootDirectory, ProjectConstants.Directory.BOOTSTRAP);

        addResourcesDirectory(contentEntry, platformBootstrapDirectory);
        // Only when bootstrap gensrc registered as source folder we can properly build the Class Hierarchy
        final var gensrcDirectory = new File(platformBootstrapDirectory, ProjectConstants.Directory.GEN_SRC);
        addSourceFolderIfNotIgnored(
            contentEntry,
            gensrcDirectory,
            JavaSourceRootType.SOURCE,
            JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
            dirsToIgnore, appSettings
        );

        excludeDirectory(contentEntry, gensrcDirectory);
        excludeDirectory(contentEntry, new File(platformBootstrapDirectory, ProjectConstants.Directory.MODEL_CLASSES));

        final var tomcat6 = new File(rootDirectory, ProjectConstants.Directory.TOMCAT_6);
        if (tomcat6.exists()) {
            excludeDirectory(contentEntry, tomcat6);
        } else {
            excludeDirectory(contentEntry, new File(rootDirectory, ProjectConstants.Directory.TOMCAT));
        }
        contentEntry.addExcludePattern("apache-ant-*");
    }

    private static void configureWebModuleRoots(
        @NotNull final YSubModuleDescriptor moduleDescriptor,
        @NotNull final ContentEntry contentEntry
    ) {
        excludeSubDirectories(
            contentEntry,
            moduleDescriptor.getModuleRootDirectory(),
            List.of(ProjectConstants.Directory.TEST_CLASSES)
        );
        configureWebInf(contentEntry, moduleDescriptor);
    }

    private static void addSourceRoots(
        @NotNull final ContentEntry contentEntry,
        @NotNull final File dir,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final ApplicationSettings applicationSettings,
        final List<String> directories,
        final JavaSourceRootType scope
    ) {
        for (final var directory : directories) {
            addSourceFolderIfNotIgnored(
                contentEntry,
                new File(dir, directory),
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
        @NotNull final File srcDir,
        @NotNull final JpsModuleSourceRootType<P> rootType,
        @NotNull final P properties,
        @NotNull final List<File> dirsToIgnore,
        @NotNull final ApplicationSettings applicationSettings
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

    private static void configureWebInf(
        final ContentEntry contentEntry,
        final YSubModuleDescriptor moduleDescriptor
    ) {
        final File rootDirectory = moduleDescriptor.getModuleRootDirectory();

        if (isCustomModuleDescriptor(moduleDescriptor)
            || (!moduleDescriptor.getRootProjectDescriptor().getImportContext().getImportOOTBModulesInReadOnlyMode() && testSrcDirectoriesExists(rootDirectory))
        ) {
            excludeDirectory(contentEntry, new File(rootDirectory, WEBROOT_WEBINF_CLASSES_PATH));
        }
    }

    private static boolean isCustomModuleDescriptor(final @NotNull ModuleDescriptor moduleDescriptor) {
        return moduleDescriptor instanceof YCustomRegularModuleDescriptor
            || (moduleDescriptor instanceof final YSubModuleDescriptor ySubModuleDescriptor && ySubModuleDescriptor.getOwner() instanceof YCustomRegularModuleDescriptor);
    }

    private static void addResourcesDirectory(final @NotNull ContentEntry contentEntry, final File platformBootstrapDirectory) {
        final var platformBootstrapResourcesDirectory = new File(platformBootstrapDirectory, ProjectConstants.Directory.RESOURCES);
        contentEntry.addSourceFolder(
            VfsUtil.pathToUrl(platformBootstrapResourcesDirectory.getAbsolutePath()),
            JavaResourceRootType.RESOURCE
        );
    }

    private static boolean testSrcDirectoriesExists(final File webModuleDirectory) {
        return ProjectConstants.Directory.TEST_SRC_DIR_NAMES.stream()
            .anyMatch(s -> new File(webModuleDirectory, s).exists());
    }
}
