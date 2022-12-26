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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.AntConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.ConfigHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.CustomHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ExtHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor;
import com.intellij.lang.ant.config.AntBuildFile;
import com.intellij.lang.ant.config.AntBuildFileBase;
import com.intellij.lang.ant.config.AntConfigurationBase;
import com.intellij.lang.ant.config.AntNoFileException;
import com.intellij.lang.ant.config.execution.AntRunConfiguration;
import com.intellij.lang.ant.config.execution.AntRunConfigurationType;
import com.intellij.lang.ant.config.impl.AllJarsUnderDirEntry;
import com.intellij.lang.ant.config.impl.AntBuildFileImpl;
import com.intellij.lang.ant.config.impl.AntClasspathEntry;
import com.intellij.lang.ant.config.impl.AntInstallation;
import com.intellij.lang.ant.config.impl.BuildFileProperty;
import com.intellij.lang.ant.config.impl.ExecuteCompositeTargetEvent;
import com.intellij.lang.ant.config.impl.GlobalAntConfiguration;
import com.intellij.lang.ant.config.impl.SinglePathEntry;
import com.intellij.lang.ant.config.impl.TargetFilter;
import com.intellij.lang.ant.config.impl.configuration.EditPropertyContainer;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.rt.ant.execution.HybrisIdeaAntLogger;
import com.intellij.util.config.AbstractProperty;
import com.intellij.util.config.ListProperty;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.ANT_OPTS;
import static java.util.Arrays.asList;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 20/10/16.
 */
public class DefaultAntConfigurator implements AntConfigurator {
    private static final Logger LOG = Logger.getInstance(DefaultAntConfigurator.class);

    public final List<String> desirablePlatformTargets = new ArrayList<>(asList(
        "clean",
        "customize",
        "all",
        "deployment",
        "build",
        "extgen",
        "modulegen",
        "initialize",
        "updatesystem",
        "yunitinit",
        "integrationtests",
        "unittests",
        "performancetests",
        "manualtests",
        "bugprooftests",
        "demotests",
        "allwebtests",
        "alltests",
        "localizationtest",
        "sonar",
        "pmd",
        "typecodetest",
        "startHybrisServer",
        "startAdminServer"
    ));
    public final List<String> desirableCustomTargets = new ArrayList<>(asList("build"));
    public final String[][] metaTargets = new String[][]{
        {"clean", "all"},
        {"clean", "customize", "all", "initialize"},
        {"clean", "customize", "all", "production"}
    };

    private HybrisProjectDescriptor hybrisProjectDescriptor;
    private PlatformHybrisModuleDescriptor platformDescriptor;
    private ConfigHybrisModuleDescriptor configDescriptor;
    private List<ExtHybrisModuleDescriptor> extHybrisModuleDescriptorList;
    private List<CustomHybrisModuleDescriptor> customHybrisModuleDescriptorList;
    private AntInstallation antInstallation;
    private AntConfigurationBase antConfiguration;
    private List<AntClasspathEntry> classPaths;

    @Override
    public void configure(
        @NotNull HybrisProjectDescriptor hybrisProjectDescriptor,
        @NotNull final List<HybrisModuleDescriptor> allModules,
        @NotNull final Project project
    ) {
        this.hybrisProjectDescriptor = hybrisProjectDescriptor;
        parseModules(allModules);
        if (platformDescriptor == null) {
            return;
        }
        final File platformDir = platformDescriptor.getRootDirectory();
        createAntInstallation(platformDir);
        if (antInstallation == null) {
            return;
        }
        createAntClassPath(platformDir);
        AntConfigurationBase.getInstance(project).setFilterTargets(true);
        antConfiguration = AntConfigurationBase.getInstance(project);
        final AntBuildFileBase buildFile = registerAntInstallation(platformDir, platformDir, desirablePlatformTargets);
        customHybrisModuleDescriptorList.forEach(
            e -> registerAntInstallation(platformDir, e.getRootDirectory(), desirableCustomTargets)
        );
        saveAntInstallation(antInstallation);
        removeMake(project);
        createMetaTargets(buildFile);
    }

    private void createMetaTargets(final AntBuildFileBase buildFile) {
        Arrays.stream(metaTargets).forEach(meta -> {
            final ExecuteCompositeTargetEvent event = new ExecuteCompositeTargetEvent(Arrays.asList(meta));
            if (antConfiguration.getTargetForEvent(event) == null) {
                antConfiguration.setTargetForEvent(buildFile, event.getMetaTargetName(), event);
            }
        });
    }

    private void parseModules(final List<HybrisModuleDescriptor> allModules) {
        platformDescriptor = null;
        configDescriptor = null;
        extHybrisModuleDescriptorList = new ArrayList<>();
        customHybrisModuleDescriptorList = new ArrayList<>();
        for (HybrisModuleDescriptor descriptor : allModules) {
            if (descriptor instanceof PlatformHybrisModuleDescriptor) {
                platformDescriptor = (PlatformHybrisModuleDescriptor) descriptor;
            }
            if (descriptor instanceof ExtHybrisModuleDescriptor) {
                extHybrisModuleDescriptorList.add((ExtHybrisModuleDescriptor) descriptor);
            }
            if (descriptor instanceof CustomHybrisModuleDescriptor) {
                customHybrisModuleDescriptorList.add((CustomHybrisModuleDescriptor) descriptor);
            }
            if (descriptor instanceof ConfigHybrisModuleDescriptor) {
                configDescriptor = (ConfigHybrisModuleDescriptor) descriptor;
            }
        }
    }

    private List<TargetFilter> getFilteredTargets(
        final AntBuildFileBase antBuildFile,
        final List<String> desirableTargets
    ) {
        return Arrays.stream(antConfiguration.getModel(antBuildFile).getTargets())
                     .map(TargetFilter::fromTarget)
                     .peek(e -> e.setVisible(desirableTargets.contains(e.getTargetName())))
                     .collect(Collectors.toList());
    }

    private AntBuildFileBase registerAntInstallation(
        final File platformDir,
        final File extensionDir,
        final List<String> desiredTargets
    ) {

        final AntBuildFileBase antBuildFile = findBuildFile(extensionDir);
        if (antBuildFile == null) {
            return null;
        }
        final List<TargetFilter> filterList = getFilteredTargets(antBuildFile, desiredTargets);
        final AbstractProperty.AbstractPropertyContainer allOptions = antBuildFile.getAllOptions();
        final EditPropertyContainer editPropertyContainer = new EditPropertyContainer(allOptions);
        setAntProperties(editPropertyContainer, platformDir, filterList);
        editPropertyContainer.apply();
        return antBuildFile;
    }

    private void setAntProperties(
        final EditPropertyContainer editPropertyContainer,
        final File platformDir,
        final List<TargetFilter> filterList
    ) {
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

        final ListProperty<BuildFileProperty> properties = AntBuildFileImpl.ANT_PROPERTIES;
        properties.getModifiableList(editPropertyContainer).clear();

        final BuildFileProperty platformHomeProperty = new BuildFileProperty();
        platformHomeProperty.setPropertyName(HybrisConstants.ANT_PLATFORM_HOME);
        platformHomeProperty.setPropertyValue(platformDir.getAbsolutePath());

        final BuildFileProperty antHomeProperty = new BuildFileProperty();
        antHomeProperty.setPropertyName(HybrisConstants.ANT_HOME);
        antHomeProperty.setPropertyValue(antInstallation.getHomeDir());

        final BuildFileProperty antOptsProperty = new BuildFileProperty();
        antOptsProperty.setPropertyName(HybrisConstants.ANT_OPTS);
        antOptsProperty.setPropertyValue(getAntOpts());

        final List<BuildFileProperty> buildFileProperties = new ArrayList<>();
        buildFileProperties.add(platformHomeProperty);
        buildFileProperties.add(antHomeProperty);
        buildFileProperties.add(antOptsProperty);

        AntBuildFileImpl.ANT_PROPERTIES.set(editPropertyContainer, buildFileProperties);
        if (hybrisProjectDescriptor.getExternalConfigDirectory() != null) {
            AntBuildFileImpl.ANT_COMMAND_LINE_PARAMETERS.set(editPropertyContainer, HybrisConstants.ANT_HYBRIS_CONFIG_DIR + hybrisProjectDescriptor.getExternalConfigDirectory().getAbsolutePath());
        }

        AntBuildFileImpl.TARGET_FILTERS.set(editPropertyContainer, filterList);
    }

    private String getAntOpts() {
        if (configDescriptor != null) {
            final File propertiesFile = new File(configDescriptor.getRootDirectory(), HybrisConstants.IMPORT_OVERRIDE_FILENAME);
            if (propertiesFile.exists()) {
                Properties properties = new Properties();
                try (InputStream in = new FileInputStream(propertiesFile)) {
                    properties.load(in);
                    String antOptsText = properties.getProperty(ANT_OPTS);
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

    private void createAntClassPath(final File platformDir) {
        classPaths = new ArrayList<>();
        //brutal hack. Do not do this at home, kids!
        //we are hiding class in a classpath to confuse the classloader and pick our implementation
        final String entry = PathManager.getResourceRoot(
            HybrisIdeaAntLogger.class, "/" + HybrisIdeaAntLogger.class.getName().replace('.', '/') + ".class"
        );
        classPaths.add(new SinglePathEntry(entry));
        //end of hack
        final File platformLibDir = new File(platformDir, HybrisConstants.LIB_DIRECTORY);
        classPaths.add(new AllJarsUnderDirEntry(platformLibDir));
        classPaths.addAll(
            extHybrisModuleDescriptorList
                .parallelStream()
                .map(e -> new AllJarsUnderDirEntry(new File(e.getRootDirectory(), HybrisConstants.LIB_DIRECTORY)))
                .toList()
        );
        final File libDir = new File(platformDir, HybrisConstants.ANT_LIB_DIR);
        classPaths.add(new AllJarsUnderDirEntry(libDir));
    }

    private AntBuildFileBase findBuildFile(final File dir) {
        final File buildxml = new File(dir, HybrisConstants.ANT_BUILD_XML);
        if (!buildxml.exists()) {
            return null;
        }

        final VirtualFile buildFile = VfsUtil.findFileByIoFile(buildxml, true);
        if (buildFile == null) {
            return null;
        }

        final AntBuildFile antBuildFile;
        try {
            antBuildFile = antConfiguration.addBuildFile(buildFile);
        } catch (AntNoFileException e) {
            return null;
        }

        if (antBuildFile instanceof AntBuildFileBase) {
            return (AntBuildFileBase) antBuildFile;
        }
        return null;
    }

    private void createAntInstallation(final File platformDir) {
        antInstallation = null;
        final String antFolderUrl;
        try {
            antFolderUrl = Files
                .find(Paths.get(platformDir.getAbsolutePath()), 1, (path, basicFileAttributes) ->
                    Files.isDirectory(path) && path.toFile().getName().matches("apache-ant.*"))
                .map(e -> e.toFile().getAbsolutePath())
                .findAny()
                .orElse(null);
        } catch (IOException e) {
            return;
        }
        if (antFolderUrl == null) {
            return;
        }
        try {
            antInstallation = AntInstallation.fromHome(antFolderUrl);
        } catch (AntInstallation.ConfigurationException ignored) {
        }
    }

    private void saveAntInstallation(final AntInstallation antInstallation) {
        final GlobalAntConfiguration globalAntConfiguration = GlobalAntConfiguration.getInstance();
        if (globalAntConfiguration == null) {
            return;
        }
        globalAntConfiguration.removeConfiguration(globalAntConfiguration.getConfiguredAnts()
                                                                         .get(antInstallation.getReference()));
        globalAntConfiguration.addConfiguration(antInstallation);
    }

    private void removeMake(final Project project) {
        final RunManagerImpl runManager = RunManagerImpl.getInstanceImpl(project);
        if (runManager == null) {
            return;
        }
        final AntRunConfigurationType antRunConfigurationType = ConfigurationTypeUtil.findConfigurationType(
            AntRunConfigurationType.class);
        final ConfigurationFactory configurationFactory = antRunConfigurationType.getConfigurationFactories()[0];
        final RunnerAndConfigurationSettings template = runManager.getConfigurationTemplate(configurationFactory);
        final AntRunConfiguration runConfiguration = (AntRunConfiguration) template.getConfiguration();
        runManager.setBeforeRunTasks(runConfiguration, Collections.<BeforeRunTask>emptyList());
    }
}
