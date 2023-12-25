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

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.AntConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.ConfigModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.PlatformModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCustomRegularModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformExtModuleDescriptor;
import com.intellij.lang.ant.config.AntBuildFileBase;
import com.intellij.lang.ant.config.AntConfigurationBase;
import com.intellij.lang.ant.config.AntNoFileException;
import com.intellij.lang.ant.config.execution.AntRunConfiguration;
import com.intellij.lang.ant.config.execution.AntRunConfigurationType;
import com.intellij.lang.ant.config.impl.*;
import com.intellij.lang.ant.config.impl.configuration.EditPropertyContainer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.ANT_OPTS;
import static java.util.Arrays.asList;

public class DefaultAntConfigurator implements AntConfigurator {
    private static final Logger LOG = Logger.getInstance(DefaultAntConfigurator.class);
    private static final Pattern PATTERN_APACHE_ANT = Pattern.compile("apache-ant.*");

    public final List<String> desirablePlatformTargets = new ArrayList<>(asList(
        "clean",
        "build",
        "all",
        "addonclean",
        "alltests",
        "allwebtests",
        "apidoc",
        "bugprooftests",
        "classpathgen",
        "cleanMavenDependencies",
        "cleanear",
        "clearAdministrationLock",
        "clearOrphanedTypes",
        "codequality",
        "commonwebclean",
        "copyFromTemplate",
        "createConfig",
        "createPlatformImageStructure",
        "createtypesystem",
        "customize",
        "demotests",
        "deploy",
        "deployDist",
        "deployDistWithSources",
        "dist",
        "distWithSources",
        "droptypesystem",
        "ear",
        "executeScript",
        "executesql",
        "extensionsxml",
        "extgen",
        "generateLicenseOverview",
        "gradle",
        "importImpex",
        "initialize",
        "initializetenantdb",
        "integrationtests",
        "localizationtest",
        "localproperties",
        "manualtests",
        "metadata",
        "modulegen",
        "performancetests",
        "production",
        "runcronjob",
        "sanitycheck",
        "sassclean",
        "sasscompile",
        "server",
        "sonarcheck",
        "sourcezip",
        "startAdminServer",
        "startHybrisServer",
        "syncaddons",
        "testMavenDependencies",
        "typecodetest",
        "unittests",
        "updateMavenDependencies",
        "updateSpringXsd",
        "updatesystem",
        "webservice_nature",
        "yunitinit",
        "yunitupdate"
    ));
    public final List<String> desirableCustomTargets = new ArrayList<>(List.of("build"));
    public final String[][] metaTargets = new String[][]{
        {"clean", "all"},
        {"clean", "customize", "all", "initialize"},
        {"clean", "customize", "all", "production"}
    };

    @Override
    public List<Function0<Unit>> configureAfterImport(
        @NotNull final HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final List<? extends ModuleDescriptor> allModules,
        @NotNull final Project project
    ) {
        final var platformDescriptor = hybrisProjectDescriptor.getPlatformHybrisModuleDescriptor();
        final var extHybrisModuleDescriptorList = new ArrayList<YPlatformExtModuleDescriptor>();
        final var customHybrisModuleDescriptorList = new ArrayList<YCustomRegularModuleDescriptor>();

        for (final var descriptor : allModules) {
            if (descriptor instanceof final YPlatformExtModuleDescriptor myDescriptor) {
                extHybrisModuleDescriptorList.add(myDescriptor);
            } else if (descriptor instanceof final YCustomRegularModuleDescriptor myDescriptor) {
                customHybrisModuleDescriptorList.add(myDescriptor);
            }
        }

        final var antInstallation = createAntInstallation(platformDescriptor);
        if (antInstallation == null) return Collections.emptyList();

        final var classPaths = createAntClassPath(platformDescriptor, extHybrisModuleDescriptorList);
        final var antConfiguration = AntConfigurationBase.getInstance(project);
        antConfiguration.setFilterTargets(true);

        final var buildFile = registerAntInstallation(hybrisProjectDescriptor, antInstallation, antConfiguration, classPaths, platformDescriptor.getModuleRootDirectory(), desirablePlatformTargets);

        if (hybrisProjectDescriptor.isImportCustomAntBuildFiles()) {
            customHybrisModuleDescriptorList.forEach(
                e -> registerAntInstallation(hybrisProjectDescriptor, antInstallation, antConfiguration, classPaths, e.getModuleRootDirectory(), desirableCustomTargets)
            );
        }
        saveAntInstallation(antInstallation);
        removeMake(project);
        createMetaTargets(antConfiguration, buildFile);

        return Collections.emptyList();
    }

    private void createMetaTargets(final AntConfigurationBase antConfiguration, final AntBuildFileBase buildFile) {
        Arrays.stream(metaTargets).forEach(meta -> {
            final var event = new ExecuteCompositeTargetEvent(Arrays.asList(meta));
            if (antConfiguration.getTargetForEvent(event) == null) {
                antConfiguration.setTargetForEvent(buildFile, event.getMetaTargetName(), event);
            }
        });
    }

    private List<TargetFilter> getFilteredTargets(
        final AntConfigurationBase antConfiguration, final AntBuildFileBase antBuildFile,
        final List<String> desirableTargets
    ) {
        return Arrays.stream(antConfiguration.getModel(antBuildFile).getTargets())
            .map(TargetFilter::fromTarget)
            .peek(e -> e.setVisible(desirableTargets.contains(e.getTargetName())))
            .collect(Collectors.toList());
    }

    private AntBuildFileBase registerAntInstallation(
        final @NotNull HybrisProjectDescriptor hybrisProjectDescriptor, final AntInstallation antInstallation, final AntConfigurationBase antConfiguration, final List<AntClasspathEntry> classPaths,
        final File extensionDir,
        final List<String> desiredTargets
    ) {

        final AntBuildFileBase antBuildFile = findBuildFile(antConfiguration, extensionDir);
        if (antBuildFile == null) {
            return null;
        }
        final var filterList = getFilteredTargets(antConfiguration, antBuildFile, desiredTargets);
        final var allOptions = antBuildFile.getAllOptions();
        final var editPropertyContainer = new EditPropertyContainer(allOptions);
        setAntProperties(hybrisProjectDescriptor, antInstallation, classPaths, editPropertyContainer, filterList);
        editPropertyContainer.apply();
        return antBuildFile;
    }

    private void setAntProperties(
        final @NotNull HybrisProjectDescriptor hybrisProjectDescriptor, final AntInstallation antInstallation, final List<AntClasspathEntry> classPaths, final EditPropertyContainer editPropertyContainer,
        final List<TargetFilter> filterList
    ) {
        final var platformDir = hybrisProjectDescriptor.getPlatformHybrisModuleDescriptor().getModuleRootDirectory();

        AntBuildFileImpl.ADDITIONAL_CLASSPATH.set(editPropertyContainer, classPaths);
        AntBuildFileImpl.TREE_VIEW.set(editPropertyContainer, true);
        AntBuildFileImpl.TREE_VIEW_ANSI_COLOR.set(editPropertyContainer, true);
        AntBuildFileImpl.TREE_VIEW_COLLAPSE_TARGETS.set(editPropertyContainer, false);
        AntBuildFileImpl.ANT_INSTALLATION.set(editPropertyContainer, antInstallation);
        AntBuildFileImpl.ANT_REFERENCE.set(editPropertyContainer, antInstallation.getReference());
        AntBuildFileImpl.RUN_WITH_ANT.set(editPropertyContainer, antInstallation);
        AntBuildFileImpl.MAX_HEAP_SIZE.set(editPropertyContainer, HybrisConstants.ANT_HEAP_SIZE_MB);
        AntBuildFileImpl.MAX_STACK_SIZE.set(editPropertyContainer, HybrisConstants.ANT_STACK_SIZE_MB);
        AntBuildFileImpl.RUN_IN_BACKGROUND.set(editPropertyContainer, false);
        AntBuildFileImpl.VERBOSE.set(editPropertyContainer, false);

        final var properties = AntBuildFileImpl.ANT_PROPERTIES;
        properties.getModifiableList(editPropertyContainer).clear();

        final var platformHomeProperty = new BuildFileProperty();
        platformHomeProperty.setPropertyName(HybrisConstants.ANT_PLATFORM_HOME);
        platformHomeProperty.setPropertyValue(platformDir.getAbsolutePath());

        final var antHomeProperty = new BuildFileProperty();
        antHomeProperty.setPropertyName(HybrisConstants.ANT_HOME);
        antHomeProperty.setPropertyValue(antInstallation.getHomeDir());

        final var antOptsProperty = new BuildFileProperty();
        antOptsProperty.setPropertyName(HybrisConstants.ANT_OPTS);
        antOptsProperty.setPropertyValue(getAntOpts(hybrisProjectDescriptor.getConfigHybrisModuleDescriptor()));

        final var buildFileProperties = List.of(
            platformHomeProperty,
            antHomeProperty,
            antOptsProperty
        );

        AntBuildFileImpl.ANT_PROPERTIES.set(editPropertyContainer, buildFileProperties);
        if (hybrisProjectDescriptor.getExternalConfigDirectory() != null) {
            AntBuildFileImpl.ANT_COMMAND_LINE_PARAMETERS.set(editPropertyContainer, HybrisConstants.ANT_HYBRIS_CONFIG_DIR + hybrisProjectDescriptor.getExternalConfigDirectory().getAbsolutePath());
        }

        AntBuildFileImpl.TARGET_FILTERS.set(editPropertyContainer, filterList);
    }

    private String getAntOpts(final @Nullable ConfigModuleDescriptor configDescriptor) {
        if (configDescriptor != null) {
            final File propertiesFile = new File(configDescriptor.getModuleRootDirectory(), HybrisConstants.IMPORT_OVERRIDE_FILENAME);
            if (propertiesFile.exists()) {
                final Properties properties = new Properties();
                try (final InputStream in = new FileInputStream(propertiesFile)) {
                    properties.load(in);
                    final String antOptsText = properties.getProperty(ANT_OPTS);
                    if (antOptsText != null && !antOptsText.trim().isEmpty()) {
                        return antOptsText.trim();
                    }
                } catch (IOException e) {
                    LOG.error("Cannot read " + HybrisConstants.IMPORT_OVERRIDE_FILENAME);
                }
            }
        }
        return HybrisConstants.ANT_XMX + HybrisConstants.ANT_HEAP_SIZE_MB + "m " + HybrisConstants.ANT_ENCODING;
    }

    private List<AntClasspathEntry> createAntClassPath(final PlatformModuleDescriptor platformDescriptor, final ArrayList<YPlatformExtModuleDescriptor> extHybrisModuleDescriptorList) {
        final var directory = platformDescriptor.getModuleRootDirectory();
        final var classPaths = new ArrayList<AntClasspathEntry>();
        final var libDir = new File(directory, HybrisConstants.ANT_LIB_DIR);
        final var platformLibDir = new File(directory, HybrisConstants.LIB_DIRECTORY);
        final var entries = extHybrisModuleDescriptorList
            .parallelStream()
            .map(e -> new AllJarsUnderDirEntry(new File(e.getModuleRootDirectory(), HybrisConstants.LIB_DIRECTORY)))
            .toList();

        classPaths.add(new AllJarsUnderDirEntry(platformLibDir));
        classPaths.add(new AllJarsUnderDirEntry(libDir));
        classPaths.addAll(entries);

        return classPaths;
    }

    private AntBuildFileBase findBuildFile(final AntConfigurationBase antConfiguration, final File dir) {
        final var buildXml = new File(dir, HybrisConstants.ANT_BUILD_XML);
        if (!buildXml.exists()) return null;

        final var buildFile = VfsUtil.findFileByIoFile(buildXml, true);
        if (buildFile == null) return null;

        try {
            final var antBuildFile = antConfiguration.addBuildFile(buildFile);

            if (antBuildFile instanceof final AntBuildFileBase antBuildFileBase) {
                return antBuildFileBase;
            }
        } catch (final AntNoFileException ignored) {
        }

        return null;
    }

    @Nullable
    private AntInstallation createAntInstallation(final PlatformModuleDescriptor platformDescriptor) {
        final String antFolderUrl;
        try {
            final var directory = platformDescriptor.getModuleRootDirectory().getAbsolutePath();
            antFolderUrl = Files
                .find(Paths.get(directory), 1, (path, basicFileAttributes) -> Files.isDirectory(path) && PATTERN_APACHE_ANT.matcher(path.toFile().getName()).matches())
                .map(Path::toFile)
                .map(File::getAbsolutePath)
                .findAny()
                .orElse(null);
        } catch (IOException e) {
            return null;
        }
        if (antFolderUrl == null) {
            return null;
        }
        try {
            return AntInstallation.fromHome(antFolderUrl);
        } catch (AntInstallation.ConfigurationException ignored) {
        }
        return null;
    }

    private void saveAntInstallation(final AntInstallation antInstallation) {
        final var globalAntConfiguration = GlobalAntConfiguration.getInstance();
        if (globalAntConfiguration == null) return;

        final var configuration = globalAntConfiguration.getConfiguredAnts().get(antInstallation.getReference());
        globalAntConfiguration.removeConfiguration(configuration);
        globalAntConfiguration.addConfiguration(antInstallation);
    }

    private void removeMake(final Project project) {
        final var runManager = RunManagerImpl.getInstanceImpl(project);
        if (runManager == null) return;

        final var antRunConfigurationType = ConfigurationTypeUtil.findConfigurationType(AntRunConfigurationType.class);
        final var configurationFactory = antRunConfigurationType.getConfigurationFactories()[0];
        final var template = runManager.getConfigurationTemplate(configurationFactory);
        final var runConfiguration = (AntRunConfiguration) template.getConfiguration();
        runManager.setBeforeRunTasks(runConfiguration, Collections.<BeforeRunTask>emptyList());
    }
}
