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

package com.intellij.idea.plugin.hybris.project.tasks;

import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.framework.FrameworkType;
import com.intellij.framework.detection.DetectionExcludesConfiguration;
import com.intellij.framework.detection.impl.FrameworkDetectionUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage;
import com.intellij.idea.plugin.hybris.project.configurators.*;
import com.intellij.idea.plugin.hybris.project.descriptors.*;
import com.intellij.idea.plugin.hybris.project.utils.ModuleGroupUtils;
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.idea.plugin.hybris.startup.HybrisProjectImportStartupActivity;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.storage.ClassPathStorageUtil;
import com.intellij.openapi.roots.impl.storage.ClasspathStorage;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleScheme;
import com.intellij.psi.codeStyle.CodeStyleSchemes;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.spellchecker.dictionary.EditableDictionary;
import com.intellij.spellchecker.dictionary.ProjectDictionary;
import com.intellij.spellchecker.dictionary.UserDictionary;
import com.intellij.spellchecker.state.ProjectDictionaryState;
import com.intellij.spring.facet.SpringFacet;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.configuration.*;
import org.jetbrains.kotlin.idea.framework.KotlinSdkType;
import org.jetbrains.kotlin.training.ift.KotlinLessonsBundle;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.*;
import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;
import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType.CUSTOM;

/**
 * Created by Martin Zdarsky-Jones on 2/11/16.
 */
public class ImportProjectProgressModalWindow extends Task.Modal {
    private static final Logger LOG = Logger.getInstance(ImportProjectProgressModalWindow.class);
    private static final int COMMITTED_CHUNK_SIZE = 20;

    private final Project project;
    private final ModifiableModuleModel model;
    private final ConfiguratorFactory configuratorFactory;
    private final HybrisProjectDescriptor hybrisProjectDescriptor;
    private final List<Module> modules;
    @NotNull private IdeModifiableModelsProvider modifiableModelsProvider;

    public ImportProjectProgressModalWindow(
        final Project project,
        final ModifiableModuleModel model,
        final ConfiguratorFactory configuratorFactory,
        final HybrisProjectDescriptor hybrisProjectDescriptor,
        final List<Module> modules
    ) {
        super(project, message("hybris.project.import.commit"), false);
        this.project = project;
        this.model = model;
        this.modifiableModelsProvider = new IdeModifiableModelsProviderImpl(project);
        this.configuratorFactory = configuratorFactory;
        this.hybrisProjectDescriptor = hybrisProjectDescriptor;
        this.modules = modules;
    }

    @Override
    public synchronized void run(@NotNull final ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        indicator.setText(message("hybris.project.import.preparation"));

        final HybrisConfiguratorCache cache = new HybrisConfiguratorCache();
        final List<HybrisModuleDescriptor> allModules = getHybrisModuleDescriptors();

        final var springConfigurator = configuratorFactory.getSpringConfigurator();
        final var groupModuleConfigurator = configuratorFactory.getGroupModuleConfigurator();
        final var versionControlSystemConfigurator = configuratorFactory.getVersionControlSystemConfigurator();

        this.initializeHybrisProjectSettings(project);
        this.updateProjectDictionary(project, hybrisProjectDescriptor.getModulesChosenForImport());
        this.selectSdk(project);
        this.saveCustomDirectoryLocation(project);
        this.saveImportedSettings(project);
        this.disableWrapOnType(ImpexLanguage.getInstance());
        PropertiesComponent.getInstance(project).setValue(PluginCommon.SHOW_UNLINKED_GRADLE_POPUP, false);

        processUltimateEdition(indicator);

        ModifiableModuleModel rootProjectModifiableModel = model == null
            ? modifiableModelsProvider.getModifiableModuleModel()
            : model;

        indicator.setText(message("hybris.project.import.spring"));
        springConfigurator.findSpringConfiguration(allModules);
        groupModuleConfigurator.findDependencyModules(allModules);
        int counter = 0;

        for (HybrisModuleDescriptor moduleDescriptor : allModules) {
            final Module javaModule = createJavaModule(indicator, rootProjectModifiableModel, moduleDescriptor, groupModuleConfigurator);
            modules.add(javaModule);
            counter++;

            if (counter >= COMMITTED_CHUNK_SIZE) {
                counter = 0;
                ApplicationManager.getApplication().invokeAndWait(
                    () -> ApplicationManager.getApplication().runWriteAction(modifiableModelsProvider::commit));

                modifiableModelsProvider = new IdeModifiableModelsProviderImpl(project);

                rootProjectModifiableModel = model == null
                    ? modifiableModelsProvider.getModifiableModuleModel()
                    : model;
            }
        }

        indicator.setText(message("hybris.project.import.dependencies"));
        indicator.setText2("");
        configuratorFactory.getModulesDependenciesConfigurator().configure(hybrisProjectDescriptor, modifiableModelsProvider);
        springConfigurator.configureDependencies(hybrisProjectDescriptor, modifiableModelsProvider);

        indicator.setText(message("hybris.project.import.runconfigurations"));
        configuratorFactory.getDebugRunConfigurationConfigurator().configure(hybrisProjectDescriptor, project, cache);

        Optional.ofNullable(configuratorFactory.getTestRunConfigurationConfigurator())
                .ifPresent(testRunConfigurationConfigurator -> testRunConfigurationConfigurator.configure(hybrisProjectDescriptor, project, cache));

        indicator.setText(message("hybris.project.import.vcs"));
        versionControlSystemConfigurator.configure(hybrisProjectDescriptor, project);

        indicator.setText(message("hybris.project.import.search.scope"));
        configuratorFactory.getSearchScopeConfigurator().configure(project, rootProjectModifiableModel);

        configureProjectIcon();

        indicator.setText(message("hybris.project.import.saving.project"));

        ApplicationManager.getApplication().invokeAndWait(
            () -> ApplicationManager.getApplication().runWriteAction(modifiableModelsProvider::commit));

        configuratorFactory.getLoadedConfigurator().configure(project, hybrisProjectDescriptor.getModulesChosenForImport());

        configureJavaCompiler(indicator, cache);
        configureKotlinCompiler(indicator, cache);
        configureEclipseModules(indicator, groupModuleConfigurator);
        configureGradleModules(indicator, groupModuleConfigurator);
        project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, Boolean.TRUE);
    }

    private void configureProjectIcon() {
        final var rootDirectory = hybrisProjectDescriptor.getRootDirectory();
        if (rootDirectory == null) return;

        final var target = Paths.get(rootDirectory.getPath(), ".idea", "icon.svg");
        final var targetDark = Paths.get(rootDirectory.getPath(), ".idea", "icon_dark.svg");

        // do not override existing Icon
        if (Files.exists(target)) return;

        if (hybrisProjectDescriptor.getProjectIconFile() == null) {
            try (final var is = this.getClass().getResourceAsStream("/icons/hybrisIcon.svg")) {
                if (is != null) {
                    Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                // NOP
            }
            // as for now, Dark icon supported only for Plugin's icons
            try (final var is = this.getClass().getResourceAsStream("/icons/hybrisIcon_dark.svg")) {
                if (is != null) {
                    Files.copy(is, targetDark, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                // NOP
            }
        } else {
            try (final var is = new FileInputStream(hybrisProjectDescriptor.getProjectIconFile())) {
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // NOP
            }
        }
    }

    private void processUltimateEdition(final @NotNull ProgressIndicator indicator) {
        if (IDEA_EDITION_ULTIMATE.equalsIgnoreCase(ApplicationNamesInfo.getInstance().getEditionName())) {
            indicator.setText(message("hybris.project.import.facets"));
            if (PluginCommon.isPluginActive(PluginCommon.SPRING_PLUGIN_ID)) {
                this.excludeFrameworkDetection(project, SpringFacet.FACET_TYPE_ID);
            }
            if (isPluginActive(JAVAEE_PLUGIN_ID)) {
                this.excludeFrameworkDetection(project, JavaeeApplicationFacet.ID);
            }
            if (isPluginActive(JAVAEE_WEB_PLUGIN_ID)) {
                this.excludeFrameworkDetection(project, WebFacet.ID);
            }
        }
    }

    @NotNull
    private Module createJavaModule(
        final @NotNull ProgressIndicator indicator,
        final ModifiableModuleModel rootProjectModifiableModel,
        final HybrisModuleDescriptor moduleDescriptor,
        final GroupModuleConfigurator groupModuleConfigurator) {
        indicator.setText(message("hybris.project.import.module.import", moduleDescriptor.getName()));
        indicator.setText2(message("hybris.project.import.module.settings"));
        final Module javaModule = rootProjectModifiableModel.newModule(
            moduleDescriptor.getIdeaModuleFile().getAbsolutePath(), StdModuleTypes.JAVA.getId()
        );

        configuratorFactory.getModuleSettingsConfigurator().configure(moduleDescriptor, javaModule);

        final ModifiableRootModel modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(javaModule);
        final ModifiableFacetModel modifiableFacetModel = modifiableModelsProvider.getModifiableFacetModel(javaModule);

        indicator.setText2(message("hybris.project.import.module.sdk"));
        ClasspathStorage.setStorageType(modifiableRootModel, ClassPathStorageUtil.DEFAULT_STORAGE);

        modifiableRootModel.inheritSdk();

        indicator.setText2(message("hybris.project.import.module.libs"));
        configuratorFactory.getLibRootsConfigurator().configure(modifiableRootModel, moduleDescriptor, modifiableModelsProvider, indicator);
        indicator.setText2(message("hybris.project.import.module.content"));

        if (shouldBeTreatedAsReadOnly(moduleDescriptor)) {
            configuratorFactory.getReadOnlyContentRootConfigurator().configure(modifiableRootModel, moduleDescriptor);
        } else {
            configuratorFactory.getRegularContentRootConfigurator().configure(modifiableRootModel, moduleDescriptor);
        }
        indicator.setText2(message("hybris.project.import.module.outputpath"));
        configuratorFactory.getCompilerOutputPathsConfigurator().configure(modifiableRootModel, moduleDescriptor);
        indicator.setText2(message("hybris.project.import.module.javadoc"));
        configuratorFactory.getJavadocModuleConfigurator().configure(modifiableRootModel, moduleDescriptor, indicator);
        indicator.setText2(message("hybris.project.import.module.groups"));
        groupModuleConfigurator.configure(rootProjectModifiableModel, javaModule, moduleDescriptor);

        indicator.setText2(message("hybris.project.import.module.facet"));
        for (final FacetConfigurator facetConfigurator : configuratorFactory.getFacetConfigurators()) {
            facetConfigurator.configure(hybrisProjectDescriptor, modifiableFacetModel, moduleDescriptor, javaModule, modifiableRootModel);
        }
        return javaModule;
    }

    private List<HybrisModuleDescriptor> getHybrisModuleDescriptors() {
        return hybrisProjectDescriptor
            .getModulesChosenForImport().stream()
            .filter(e -> !(e instanceof MavenModuleDescriptor)
                         && !(e instanceof EclipseModuleDescriptor)
                         && !(e instanceof GradleModuleDescriptor)
            )
            .collect(Collectors.toList());
    }

    private void configureJavaCompiler(final @NotNull ProgressIndicator indicator, final HybrisConfiguratorCache cache) {
        final JavaCompilerConfigurator compilerConfigurator = configuratorFactory.getJavaCompilerConfigurator();

        if (compilerConfigurator == null) return;

        indicator.setText(message("hybris.project.import.compiler.java"));
        compilerConfigurator.configure(hybrisProjectDescriptor, project, cache);
    }

    private void configureKotlinCompiler(final @NotNull ProgressIndicator indicator, final HybrisConfiguratorCache cache) {
        final var compilerConfigurator = configuratorFactory.getKotlinCompilerConfigurator();

        if (compilerConfigurator == null) return;

        indicator.setText(message("hybris.project.import.compiler.kotlin"));
        compilerConfigurator.configure(hybrisProjectDescriptor, project, cache);
    }

    private void configureEclipseModules(final @NotNull ProgressIndicator indicator, final GroupModuleConfigurator groupModuleConfigurator) {
        final EclipseConfigurator eclipseConfigurator = configuratorFactory.getEclipseConfigurator();

        if (eclipseConfigurator == null) return;

        indicator.setText(message("hybris.project.import.eclipse"));

        try {
            final List<EclipseModuleDescriptor> eclipseModules = hybrisProjectDescriptor
                .getModulesChosenForImport()
                .stream()
                .filter(EclipseModuleDescriptor.class::isInstance)
                .map(EclipseModuleDescriptor.class::cast)
                .collect(Collectors.toList());
            if (!eclipseModules.isEmpty()) {
                Map<String, String[]> eclipseGroupMapping = ModuleGroupUtils.fetchGroupMapping(groupModuleConfigurator, eclipseModules);
                eclipseConfigurator.configure(hybrisProjectDescriptor, project, eclipseModules, eclipseGroupMapping);
            }
        } catch (Exception e) {
            LOG.error("Can not import Eclipse modules due to an error.", e);
        }
    }

    private void configureGradleModules(final @NotNull ProgressIndicator indicator, final GroupModuleConfigurator groupModuleConfigurator) {
        final GradleConfigurator gradleConfigurator = configuratorFactory.getGradleConfigurator();

        if (gradleConfigurator == null) return;

        indicator.setText(message("hybris.project.import.gradle"));

        try {
            final List<GradleModuleDescriptor> gradleModules = hybrisProjectDescriptor
                .getModulesChosenForImport()
                .stream()
                .filter(GradleModuleDescriptor.class::isInstance)
                .map(GradleModuleDescriptor.class::cast)
                .collect(Collectors.toList());
            if (!gradleModules.isEmpty()) {
                final Map<String, String[]> gradleRootGroupMapping = ModuleGroupUtils.fetchGroupMapping(groupModuleConfigurator, gradleModules);
                gradleConfigurator.configure(hybrisProjectDescriptor, project, gradleModules, gradleRootGroupMapping);
            }
        } catch (Exception e) {
            LOG.error("Can not import Gradle modules due to an error.", e);
        }
    }

    private void updateProjectDictionary(
        final Project project,
        final List<HybrisModuleDescriptor> modules
    ) {
        final ProjectDictionaryState dictionaryState = project.getService(ProjectDictionaryState.class);
        final ProjectDictionary projectDictionary = dictionaryState.getProjectDictionary();
        projectDictionary.getEditableWords();//ensure dictionaries exist
        EditableDictionary hybrisDictionary = projectDictionary.getDictionaries().stream()
                    .filter(e -> DICTIONARY_NAME.equals(e.getName())).findFirst().orElse(null);
        if (hybrisDictionary == null) {
            hybrisDictionary = new UserDictionary(DICTIONARY_NAME);
            projectDictionary.getDictionaries().add(hybrisDictionary);
        }
        hybrisDictionary.addToDictionary(DICTIONARY_WORDS);
        hybrisDictionary.addToDictionary(project.getName().toLowerCase());
        final Set<String> moduleNames = modules.stream()
                                               .map(HybrisModuleDescriptor::getName)
                                               .map(String::toLowerCase)
                                               .collect(Collectors.toSet());
        hybrisDictionary.addToDictionary(moduleNames);
    }

    private void initializeHybrisProjectSettings(@NotNull final Project project) {
        Validate.notNull(project);

        final @NotNull HybrisProjectSettings hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project)
                                                                                                   .getState();
        hybrisProjectSettings.setHybrisProject(true);
        final IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID));

        if (plugin == null) return;

        final String version = plugin.getVersion();
        hybrisProjectSettings.setImportedByVersion(version);
    }

    private void selectSdk(@NotNull final Project project) {
        Validate.notNull(project);

        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

        final Sdk projectSdk = projectRootManager.getProjectSdk();

        if (null == projectSdk) {
            return;
        }

        if (StringUtils.isNotBlank(projectSdk.getVersionString())) {
            final JavaSdkVersion sdkVersion = JavaSdkVersion.fromVersionString(projectSdk.getVersionString());
            final LanguageLevelProjectExtension languageLevelExt = LanguageLevelProjectExtension.getInstance(project);

            if (sdkVersion != null && sdkVersion.getMaxLanguageLevel() != languageLevelExt.getLanguageLevel()) {
                languageLevelExt.setLanguageLevel(sdkVersion.getMaxLanguageLevel());
            }
        }
    }

    private void saveCustomDirectoryLocation(final Project project) {
        final HybrisProjectSettings hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project)
                                                                                          .getState();
        final File customDirectory = hybrisProjectDescriptor.getExternalExtensionsDirectory();
        final File hybrisDirectory = hybrisProjectDescriptor.getHybrisDistributionDirectory();
        final VirtualFile projectDir = ProjectUtil.guessProjectDir(project);

        if (projectDir == null) return;

        final File baseDirectory = VfsUtilCore.virtualToIoFile(projectDir);
        final Path projectPath = Paths.get(baseDirectory.getAbsolutePath());
        final Path hybrisPath = Paths.get(hybrisDirectory.getAbsolutePath());
        final Path relativeHybrisPath = projectPath.relativize(hybrisPath);
        hybrisProjectSettings.setHybrisDirectory(relativeHybrisPath.toString());
        if (customDirectory != null) {
            final Path customPath = Paths.get(customDirectory.getAbsolutePath());
            final Path relativeCustomPath = hybrisPath.relativize(customPath);
            hybrisProjectSettings.setCustomDirectory(relativeCustomPath.toString());
        }
    }

    private void saveImportedSettings(final Project project) {
        final var hybrisSettingsComponent = HybrisProjectSettingsComponent.getInstance(project);
        final var hybrisProjectSettings = hybrisSettingsComponent.getState();
        final var appSettings = HybrisApplicationSettingsComponent.getInstance().getState();
        hybrisProjectSettings.setImportOotbModulesInReadOnlyMode(hybrisProjectDescriptor.isImportOotbModulesInReadOnlyMode());
        final File extDir = hybrisProjectDescriptor.getExternalExtensionsDirectory();
        if (extDir != null && extDir.isDirectory()) {
            hybrisProjectSettings.setExternalExtensionsDirectory(FileUtil.toSystemIndependentName(extDir.getPath()));
        }
        File configDir = hybrisProjectDescriptor.getExternalConfigDirectory();
        if (configDir != null && configDir.isDirectory()) {
            hybrisProjectSettings.setExternalConfigDirectory(FileUtil.toSystemIndependentName(configDir.getPath()));
        }
        final ConfigHybrisModuleDescriptor configModule = hybrisProjectDescriptor.getConfigHybrisModuleDescriptor();
        if (configModule != null) {
            configDir = configModule.getRootDirectory();
            if (configDir.isDirectory()) {
                hybrisProjectSettings.setConfigDirectory(FileUtil.toSystemIndependentName(configDir.getPath()));
            }
        }
        final File dbDriversDir = hybrisProjectDescriptor.getExternalDbDriversDirectory();
        if (dbDriversDir != null && dbDriversDir.isDirectory()) {
            hybrisProjectSettings.setExternalDbDriversDirectory(FileUtil.toSystemIndependentName(dbDriversDir.getPath()));
            appSettings.setExternalDbDriversDirectory(FileUtil.toSystemIndependentName(dbDriversDir.getPath()));
        } else {
            appSettings.setExternalDbDriversDirectory("");
        }

        appSettings.setWithMavenSources(hybrisProjectDescriptor.isWithMavenSources());
        appSettings.setWithMavenJavadocs(hybrisProjectDescriptor.isWithMavenJavadocs());
        appSettings.setWithStandardProvidedSources(hybrisProjectDescriptor.isWithStandardProvidedSources());

        hybrisProjectSettings.setCreateBackwardCyclicDependenciesForAddOns(hybrisProjectDescriptor.isCreateBackwardCyclicDependenciesForAddOn());
        final File sourceCodeFile = hybrisProjectDescriptor.getSourceCodeFile();

        if (sourceCodeFile != null && sourceCodeFile.exists()) {
            hybrisProjectSettings.setSourceCodeFile(FileUtil.toSystemIndependentName(sourceCodeFile.getPath()));
            final boolean directory = sourceCodeFile.isDirectory();
            appSettings.setSourceCodeDirectory(FileUtil.toSystemIndependentName(
                directory ? sourceCodeFile.getPath() : sourceCodeFile.getParent()));
            appSettings.setSourceZipUsed(!directory);
        }
        final File modulesFilesDirectory = hybrisProjectDescriptor.getModulesFilesDirectory();
        if (modulesFilesDirectory != null && modulesFilesDirectory.isDirectory()) {
            hybrisProjectSettings.setIdeModulesFilesDirectory(FileUtil.toSystemIndependentName(modulesFilesDirectory.getPath()));
        }
        hybrisProjectSettings.setFollowSymlink(hybrisProjectDescriptor.isFollowSymlink());
        hybrisProjectSettings.setScanThroughExternalModule(hybrisProjectDescriptor.isScanThroughExternalModule());
        hybrisProjectSettings.setModulesOnBlackList(createModulesOnBlackList());
        hybrisProjectSettings.setHybrisVersion(hybrisProjectDescriptor.getHybrisVersion());
        hybrisProjectSettings.setJavadocUrl(hybrisProjectDescriptor.getJavadocUrl());
        final var completeSetOfHybrisModules = hybrisProjectDescriptor.getFoundModules().stream()
                               .filter(e -> !(e instanceof MavenModuleDescriptor)
                                            && !(e instanceof EclipseModuleDescriptor)
                                            && !(e instanceof GradleModuleDescriptor)
                                            && !(e instanceof ConfigHybrisModuleDescriptor)
                               )
                               .collect(Collectors.toSet());
        hybrisSettingsComponent.setAvailableExtensions(completeSetOfHybrisModules);
        hybrisProjectSettings.setCompleteSetOfAvailableExtensionsInHybris(completeSetOfHybrisModules.stream()
                                                                              .map(HybrisModuleDescriptor::getName)
                                                                              .collect(Collectors.toSet()));
        hybrisProjectSettings.setExcludeTestSources(hybrisProjectDescriptor.isExcludeTestSources());

        CommonIdeaService.getInstance().fixRemoteConnectionSettings(project);

        project.putUserData(HybrisProjectImportStartupActivity.Companion.getSyncProjectSettingsKey(), true);
    }

    private Set<String> createModulesOnBlackList() {
        final List<String> toBeImportedNames = hybrisProjectDescriptor
            .getModulesChosenForImport().stream()
            .map(HybrisModuleDescriptor::getName)
            .toList();
        return hybrisProjectDescriptor
            .getFoundModules().stream()
            .filter(e -> !hybrisProjectDescriptor.getModulesChosenForImport().contains(e))
            .filter(e -> toBeImportedNames.contains(e.getName()))
            .map(HybrisModuleDescriptor::getRelativePath)
            .collect(Collectors.toSet());
    }

    private void disableWrapOnType(final Language impexLanguage) {
        final CodeStyleScheme currentScheme = CodeStyleSchemes.getInstance().getCurrentScheme();
        final CodeStyleSettings codeStyleSettings = currentScheme.getCodeStyleSettings();
        if (impexLanguage != null) {
            CommonCodeStyleSettings langSettings = codeStyleSettings.getCommonSettings(impexLanguage);
            langSettings.WRAP_ON_TYPING = CommonCodeStyleSettings.WrapOnTyping.NO_WRAP.intValue;
        }
    }

    private void excludeFrameworkDetection(final Project project, FacetTypeId facetTypeId) {
        final DetectionExcludesConfiguration configuration = DetectionExcludesConfiguration.getInstance(project);
        final FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(facetTypeId);
        final FrameworkType frameworkType = FrameworkDetectionUtil.findFrameworkTypeForFacetDetector(facetType);

        if (frameworkType != null) {
            configuration.addExcludedFramework(frameworkType);
        }
    }

    private boolean shouldBeTreatedAsReadOnly(final HybrisModuleDescriptor moduleDescriptor) {
        if (moduleDescriptor.getDescriptorType() == CUSTOM) {
            return false;
        }
        return moduleDescriptor.getRootProjectDescriptor().isImportOotbModulesInReadOnlyMode();
    }
}
