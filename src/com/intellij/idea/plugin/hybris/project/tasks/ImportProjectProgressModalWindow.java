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

package com.intellij.idea.plugin.hybris.project.tasks;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.framework.detection.DetectionExcludesConfiguration;
import com.intellij.framework.detection.impl.FrameworkDetectionUtil;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage;
import com.intellij.idea.plugin.hybris.project.configurators.*;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*;
import com.intellij.idea.plugin.hybris.project.utils.Plugin;
import com.intellij.idea.plugin.hybris.settings.ApplicationSettings;
import com.intellij.idea.plugin.hybris.settings.ProjectSettings;
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent;
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
import com.intellij.spellchecker.dictionary.ProjectDictionary;
import com.intellij.spellchecker.dictionary.UserDictionary;
import com.intellij.spellchecker.state.ProjectDictionaryState;
import com.intellij.spring.facet.SpringFacet;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.*;
import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;

public class ImportProjectProgressModalWindow extends Task.Modal {
    private static final Logger LOG = Logger.getInstance(ImportProjectProgressModalWindow.class);
    private static final int COMMITTED_CHUNK_SIZE = 20;
    private static final String SHOW_UNLINKED_GRADLE_POPUP = "show.inlinked.gradle.project.popup";

    private final Project project;
    private final ModifiableModuleModel model;
    private final ConfiguratorFactory configuratorFactory;
    private final HybrisProjectDescriptor hybrisProjectDescriptor;
    private final List<Module> modules;
    private final boolean refresh;
    @NotNull
    private IdeModifiableModelsProvider modifiableModelsProvider;

    public ImportProjectProgressModalWindow(
        final Project project,
        final ModifiableModuleModel model,
        final ConfiguratorFactory configuratorFactory,
        final HybrisProjectDescriptor hybrisProjectDescriptor,
        final List<Module> modules,
        final boolean refresh) {
        super(project, message("hybris.project.import.commit"), false);
        this.project = project;
        this.model = model;
        this.modifiableModelsProvider = new IdeModifiableModelsProviderImpl(project);
        this.configuratorFactory = configuratorFactory;
        this.hybrisProjectDescriptor = hybrisProjectDescriptor;
        this.modules = modules;
        this.refresh = refresh;
    }

    @Override
    public synchronized void run(@NotNull final ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        indicator.setText(message("hybris.project.import.preparation"));

        final var cache = new HybrisConfiguratorCache();
        final var allModules = getHybrisModuleDescriptors();
        final var allYModules = allModules.stream()
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .distinct()
            .collect(Collectors.toMap(YModuleDescriptor::getName, Function.identity()));
        final var allModuleDescriptors = allModules.stream()
            .collect(Collectors.toMap(ModuleDescriptor::getName, Function.identity()));
        final var appSettings = ApplicationSettingsComponent.getInstance().getState();

        final var projectSettingsComponent = ProjectSettingsComponent.getInstance(project);
        final var projectSettings = projectSettingsComponent.getState();

        final var groupModuleConfigurator = configuratorFactory.getGroupModuleConfigurator();
        final var modulesFilesDirectory = hybrisProjectDescriptor.getModulesFilesDirectory();
        if (modulesFilesDirectory != null && !modulesFilesDirectory.exists()) {
            modulesFilesDirectory.mkdirs();
        }

        this.initializeHybrisProjectSettings(project, projectSettings);
        this.updateProjectDictionary(project, hybrisProjectDescriptor.getModulesChosenForImport());
        this.selectSdk(project);

        if (!refresh) {
            this.saveCustomDirectoryLocation(project, projectSettings);
            projectSettings.setExcludedFromScanning(hybrisProjectDescriptor.getExcludedFromScanning());
        }

        this.saveImportedSettings(projectSettings, appSettings, projectSettingsComponent);
        this.disableWrapOnType(ImpexLanguage.INSTANCE);

        PropertiesComponent.getInstance(project).setValue(SHOW_UNLINKED_GRADLE_POPUP, false);

        processUltimateEdition(indicator);

        ModifiableModuleModel rootProjectModifiableModel = model == null
            ? modifiableModelsProvider.getModifiableModuleModel()
            : model;

        configuratorFactory.getSpringConfigurator().process(indicator, hybrisProjectDescriptor, allModuleDescriptors);
        groupModuleConfigurator.process(indicator, allModules);

        int counter = 0;

        for (ModuleDescriptor moduleDescriptor : allModules) {
            final Module javaModule = createJavaModule(indicator, allYModules, rootProjectModifiableModel, moduleDescriptor, appSettings);
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

        configuratorFactory.getModuleDependenciesConfigurator().configure(indicator, hybrisProjectDescriptor, modifiableModelsProvider);
        configuratorFactory.getSpringConfigurator().configure(indicator, hybrisProjectDescriptor, allModuleDescriptors, modifiableModelsProvider);
        configuratorFactory.getRunConfigurationConfigurator().configure(indicator, hybrisProjectDescriptor, project, cache);
        configuratorFactory.getVersionControlSystemConfigurator().configure(indicator, hybrisProjectDescriptor, project);
        configuratorFactory.getSearchScopeConfigurator().configure(indicator, project, appSettings, rootProjectModifiableModel);

        configureProjectIcon();

        indicator.setText(message("hybris.project.import.saving.project"));

        ApplicationManager.getApplication().invokeAndWait(
            () -> ApplicationManager.getApplication().runWriteAction(modifiableModelsProvider::commit));

        configuratorFactory.getLoadedConfigurator().configure(project, hybrisProjectDescriptor.getModulesChosenForImport());

        configureJavaCompiler(indicator, cache);
        configureKotlinCompiler(indicator, cache);
        configureEclipseModules(indicator);
        configureGradleModules(indicator);
        configureAngularModules(indicator, groupModuleConfigurator, appSettings);

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
            if (Plugin.SPRING.isActive()) {
                this.excludeFrameworkDetection(project, SpringFacet.FACET_TYPE_ID);
            }
            if (Plugin.JAVAEE.isActive()) {
                this.excludeFrameworkDetection(project, JavaeeApplicationFacet.ID);
            }
            if (Plugin.JAVAEE_WEB.isActive()) {
                this.excludeFrameworkDetection(project, WebFacet.ID);
            }
        }
    }

    @NotNull
    private Module createJavaModule(final @NotNull ProgressIndicator indicator,
                                    final Map<String, YModuleDescriptor> allYModules,
                                    final ModifiableModuleModel rootProjectModifiableModel,
                                    final ModuleDescriptor moduleDescriptor,
                                    final @NotNull ApplicationSettings appSettings
    ) {
        indicator.setText(message("hybris.project.import.module.import", moduleDescriptor.getName()));
        indicator.setText2(message("hybris.project.import.module.settings"));

        final Module javaModule = rootProjectModifiableModel.newModule(
            moduleDescriptor.ideaModuleFile().getAbsolutePath(),
            StdModuleTypes.JAVA.getId()
        );

        configuratorFactory.getModuleSettingsConfigurator().configure(moduleDescriptor, javaModule);

        final var modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(javaModule);

        indicator.setText2(message("hybris.project.import.module.sdk"));
        ClasspathStorage.setStorageType(modifiableRootModel, ClassPathStorageUtil.DEFAULT_STORAGE);

        modifiableRootModel.inheritSdk();

        configuratorFactory.getJavadocSettingsConfigurator().configure(modifiableRootModel, moduleDescriptor);
        configuratorFactory.getLibRootsConfigurator().configure(indicator, allYModules, modifiableRootModel, moduleDescriptor, modifiableModelsProvider, indicator);
        configuratorFactory.getContentRootConfigurator().configure(indicator, modifiableRootModel, moduleDescriptor, appSettings);
        configuratorFactory.getCompilerOutputPathsConfigurator().configure(indicator, modifiableRootModel, moduleDescriptor);

        indicator.setText2(message("hybris.project.import.module.facet"));
        configureModuleFacet(moduleDescriptor, javaModule, modifiableRootModel, modifiableModelsProvider);
        return javaModule;
    }

    private void configureModuleFacet(
        final ModuleDescriptor moduleDescriptor, final Module module,
        final ModifiableRootModel modifiableRootModel, final IdeModifiableModelsProvider modifiableModelsProvider
    ) {
        final var modifiableFacetModel = modifiableModelsProvider.getModifiableFacetModel(module);

        for (final var facetConfigurator : configuratorFactory.getFacetConfigurators()) {
            facetConfigurator.configure(hybrisProjectDescriptor, modifiableFacetModel, moduleDescriptor, module, modifiableRootModel);
        }
    }

    private List<ModuleDescriptor> getHybrisModuleDescriptors() {
        return hybrisProjectDescriptor.getModulesChosenForImport().stream()
            .filter(e -> !(e instanceof MavenModuleDescriptor)
                && !(e instanceof EclipseModuleDescriptor)
                && !(e instanceof GradleModuleDescriptor)
                && !(e instanceof AngularModuleDescriptor)
            )
            .toList();
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
        compilerConfigurator.configure(hybrisProjectDescriptor, project);
    }

    private void configureEclipseModules(final @NotNull ProgressIndicator indicator) {
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
                eclipseConfigurator.configure(hybrisProjectDescriptor, project, eclipseModules);
            }
        } catch (Exception e) {
            LOG.error("Can not import Eclipse modules due to an error.", e);
        }
    }

    private void configureGradleModules(final @NotNull ProgressIndicator indicator) {
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
                gradleConfigurator.configure(project, gradleModules);
            }
        } catch (Exception e) {
            LOG.error("Can not import Gradle modules due to an error.", e);
        }
    }

    private void configureAngularModules(
        final @NotNull ProgressIndicator indicator,
        final GroupModuleConfigurator groupModuleConfigurator,
        final ApplicationSettings appSettings
    ) {
        final var configurator = configuratorFactory.getAngularConfigurator();

        if (configurator == null) return;

        indicator.setText(message("hybris.project.import.angular"));

        final var contentRootConfigurator = configuratorFactory.getContentRootConfigurator();
        final var modifiableModelsProvider = new IdeModifiableModelsProviderImpl(project);
        final var rootProjectModifiableModel = model == null
            ? modifiableModelsProvider.getModifiableModuleModel()
            : model;

        final var modules = hybrisProjectDescriptor
            .getModulesChosenForImport()
            .stream()
            .filter(AngularModuleDescriptor.class::isInstance)
            .map(AngularModuleDescriptor.class::cast)
            .peek(module -> groupModuleConfigurator.process(module, module.getDirectDependencies()))
            .collect(Collectors.toMap(Function.identity(), module -> rootProjectModifiableModel.newModule(
                module.ideaModuleFile().getAbsolutePath(),
                StdModuleTypes.JAVA.getId()
            )));

        modules.forEach((descriptor, module) -> {
            final var modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(module);

            contentRootConfigurator.configure(indicator, modifiableRootModel, descriptor, appSettings);
            configureModuleFacet(descriptor, module, modifiableRootModel, modifiableModelsProvider);
        });

        ApplicationManager.getApplication().invokeAndWait(
            () -> ApplicationManager.getApplication().runWriteAction(modifiableModelsProvider::commit));

        configurator.configure(project, modules);
    }

    private void updateProjectDictionary(
        final Project project,
        final List<ModuleDescriptor> modules
    ) {
        final ProjectDictionaryState dictionaryState = project.getService(ProjectDictionaryState.class);
        final ProjectDictionary projectDictionary = dictionaryState.getProjectDictionary();
        projectDictionary.getEditableWords();//ensure dictionaries exist
        final var hybrisDictionary = projectDictionary.getDictionaries().stream()
            .filter(e -> DICTIONARY_NAME.equals(e.getName()))
            .findFirst()
            .orElseGet(() -> {
                final var dictionary = new UserDictionary(DICTIONARY_NAME);
                projectDictionary.getDictionaries().add(dictionary);
                return dictionary;
            });
        hybrisDictionary.addToDictionary(DICTIONARY_WORDS);
        hybrisDictionary.addToDictionary(project.getName().toLowerCase());
        final Set<String> moduleNames = modules.stream()
            .map(ModuleDescriptor::getName)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        hybrisDictionary.addToDictionary(moduleNames);
    }

    private void initializeHybrisProjectSettings(@NotNull final Project project, final @NotNull ProjectSettings projectSettings) {
        projectSettings.setHybrisProject(true);
        final IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(HybrisConstants.PLUGIN_ID));

        if (plugin == null) return;

        final String version = plugin.getVersion();
        projectSettings.setImportedByVersion(version);
    }

    private void selectSdk(@NotNull final Project project) {
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

    private void saveCustomDirectoryLocation(final Project project, final ProjectSettings projectSettings) {
        final File customDirectory = hybrisProjectDescriptor.getExternalExtensionsDirectory();
        final File hybrisDirectory = hybrisProjectDescriptor.getHybrisDistributionDirectory();
        final VirtualFile projectDir = ProjectUtil.guessProjectDir(project);

        if (projectDir == null) return;

        final File baseDirectory = VfsUtilCore.virtualToIoFile(projectDir);
        final Path projectPath = Paths.get(baseDirectory.getAbsolutePath());
        final Path hybrisPath = Paths.get(hybrisDirectory.getAbsolutePath());
        final Path relativeHybrisPath = projectPath.relativize(hybrisPath);
        projectSettings.setHybrisDirectory(relativeHybrisPath.toString());
        if (customDirectory != null) {
            final Path customPath = Paths.get(customDirectory.getAbsolutePath());
            final Path relativeCustomPath = hybrisPath.relativize(customPath);
            projectSettings.setCustomDirectory(relativeCustomPath.toString());
        }
    }

    private void saveImportedSettings(@NotNull final ProjectSettings projectSettings,
                                      @NotNull final ApplicationSettings appSettings,
                                      @NotNull final ProjectSettingsComponent hybrisSettingsComponent) {
        projectSettings.setImportOotbModulesInReadOnlyMode(hybrisProjectDescriptor.isImportOotbModulesInReadOnlyMode());
        final File extDir = hybrisProjectDescriptor.getExternalExtensionsDirectory();
        if (extDir != null && extDir.isDirectory()) {
            projectSettings.setExternalExtensionsDirectory(FileUtil.toSystemIndependentName(extDir.getPath()));
        }
        File configDir = hybrisProjectDescriptor.getExternalConfigDirectory();
        if (configDir != null && configDir.isDirectory()) {
            projectSettings.setExternalConfigDirectory(FileUtil.toSystemIndependentName(configDir.getPath()));
        }
        final ConfigModuleDescriptor configModule = hybrisProjectDescriptor.getConfigHybrisModuleDescriptor();
        if (configModule != null) {
            configDir = configModule.getModuleRootDirectory();
            if (configDir.isDirectory()) {
                projectSettings.setConfigDirectory(FileUtil.toSystemIndependentName(configDir.getPath()));
            }
        }
        final File dbDriversDir = hybrisProjectDescriptor.getExternalDbDriversDirectory();
        if (dbDriversDir != null && dbDriversDir.isDirectory()) {
            projectSettings.setExternalDbDriversDirectory(FileUtil.toSystemIndependentName(dbDriversDir.getPath()));
            appSettings.setExternalDbDriversDirectory(FileUtil.toSystemIndependentName(dbDriversDir.getPath()));
        } else {
            appSettings.setExternalDbDriversDirectory("");
        }

        appSettings.setIgnoreNonExistingSourceDirectories(hybrisProjectDescriptor.isIgnoreNonExistingSourceDirectories());
        appSettings.setWithStandardProvidedSources(hybrisProjectDescriptor.isWithStandardProvidedSources());

        final File sourceCodeFile = hybrisProjectDescriptor.getSourceCodeFile();

        if (sourceCodeFile != null && sourceCodeFile.exists()) {
            projectSettings.setSourceCodeFile(FileUtil.toSystemIndependentName(sourceCodeFile.getPath()));
            final boolean directory = sourceCodeFile.isDirectory();
            appSettings.setSourceCodeDirectory(FileUtil.toSystemIndependentName(
                directory ? sourceCodeFile.getPath() : sourceCodeFile.getParent()));
            appSettings.setSourceZipUsed(!directory);
        }
        final File modulesFilesDirectory = hybrisProjectDescriptor.getModulesFilesDirectory();
        if (modulesFilesDirectory != null && modulesFilesDirectory.isDirectory()) {
            projectSettings.setIdeModulesFilesDirectory(FileUtil.toSystemIndependentName(modulesFilesDirectory.getPath()));
        }
        projectSettings.setFollowSymlink(hybrisProjectDescriptor.isFollowSymlink());
        projectSettings.setScanThroughExternalModule(hybrisProjectDescriptor.isScanThroughExternalModule());
        projectSettings.setModulesOnBlackList(createModulesOnBlackList());
        projectSettings.setHybrisVersion(hybrisProjectDescriptor.getHybrisVersion());

        final var credentialAttributes = new CredentialAttributes(HybrisConstants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN);
        PasswordSafe.getInstance().setPassword(credentialAttributes, hybrisProjectDescriptor.getCCv2Token());

        projectSettings.setJavadocUrl(hybrisProjectDescriptor.getJavadocUrl());
        final var completeSetOfHybrisModules = hybrisProjectDescriptor.getFoundModules().stream()
            .filter(e -> !(e instanceof MavenModuleDescriptor)
                && !(e instanceof EclipseModuleDescriptor)
                && !(e instanceof GradleModuleDescriptor)
                && !(e instanceof CCv2ModuleDescriptor)
                && !(e instanceof ConfigModuleDescriptor)
                && !(e instanceof YSubModuleDescriptor)
            )
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .collect(Collectors.toSet());
        hybrisSettingsComponent.setAvailableExtensions(completeSetOfHybrisModules);
        projectSettings.setCompleteSetOfAvailableExtensionsInHybris(completeSetOfHybrisModules.stream()
            .map(ModuleDescriptor::getName)
            .collect(Collectors.toSet()));
        projectSettings.setExcludeTestSources(hybrisProjectDescriptor.isExcludeTestSources());
    }

    private Set<String> createModulesOnBlackList() {
        final List<String> toBeImportedNames = hybrisProjectDescriptor
            .getModulesChosenForImport().stream()
            .map(ModuleDescriptor::getName)
            .toList();
        return hybrisProjectDescriptor
            .getFoundModules().stream()
            .filter(e -> !hybrisProjectDescriptor.getModulesChosenForImport().contains(e))
            .filter(e -> toBeImportedNames.contains(e.getName()))
            .map(ModuleDescriptor::getRelativePath)
            .collect(Collectors.toSet());
    }

    private void disableWrapOnType(final Language impexLanguage) {
        final CodeStyleScheme currentScheme = CodeStyleSchemes.getInstance().getCurrentScheme();
        final CodeStyleSettings codeStyleSettings = currentScheme.getCodeStyleSettings();
        if (impexLanguage != null) {
            final CommonCodeStyleSettings langSettings = codeStyleSettings.getCommonSettings(impexLanguage);
            langSettings.WRAP_ON_TYPING = CommonCodeStyleSettings.WrapOnTyping.NO_WRAP.intValue;
        }
    }

    private void excludeFrameworkDetection(final Project project, final FacetTypeId facetTypeId) {
        final var configuration = DetectionExcludesConfiguration.getInstance(project);
        final var facetType = FacetTypeRegistry.getInstance().findFacetType(facetTypeId);
        final var frameworkType = FrameworkDetectionUtil.findFrameworkTypeForFacetDetector(facetType);

        if (frameworkType != null) {
            configuration.addExcludedFramework(frameworkType);
        }
    }

}
