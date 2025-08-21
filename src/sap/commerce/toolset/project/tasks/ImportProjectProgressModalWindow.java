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

package sap.commerce.toolset.project.tasks;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.framework.detection.DetectionExcludesConfiguration;
import com.intellij.framework.detection.impl.FrameworkDetectionUtil;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.diagnostic.Logger;
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
import sap.commerce.toolset.Plugin;
import sap.commerce.toolset.ccv2.CCv2Constants;
import sap.commerce.toolset.impex.ImpExLanguage;
import sap.commerce.toolset.project.ModuleGroupingUtil;
import sap.commerce.toolset.project.configurator.ConfiguratorCache;
import sap.commerce.toolset.project.configurators.ConfiguratorFactory;
import sap.commerce.toolset.project.configurators.JavaCompilerConfigurator;
import sap.commerce.toolset.project.descriptor.*;
import sap.commerce.toolset.project.descriptor.impl.AngularModuleDescriptor;
import sap.commerce.toolset.project.descriptor.impl.ExternalModuleDescriptor;
import sap.commerce.toolset.project.settings.ProjectSettings;
import sap.commerce.toolset.settings.ApplicationSettings;
import sap.commerce.toolset.settings.WorkspaceSettings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static sap.commerce.toolset.HybrisConstants.*;
import static sap.commerce.toolset.HybrisI18NBundleUtils.message;

public class ImportProjectProgressModalWindow extends Task.Modal {
    private static final Logger LOG = Logger.getInstance(ImportProjectProgressModalWindow.class);
    private static final int COMMITTED_CHUNK_SIZE = 20;

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

        final var cache = new ConfiguratorCache();
        final var allHybrisModules = getHybrisModuleDescriptors();
        final var allYModules = allHybrisModules.stream()
            .filter(YModuleDescriptor.class::isInstance)
            .map(YModuleDescriptor.class::cast)
            .distinct()
            .collect(Collectors.toMap(YModuleDescriptor::getName, Function.identity()));
        final var allHybrisModuleDescriptors = allHybrisModules.stream()
            .collect(Collectors.toMap(ModuleDescriptor::getName, Function.identity()));
        final var appSettings = ApplicationSettings.getInstance();

        final var projectSettings = ProjectSettings.getInstance(project);

        final var modulesFilesDirectory = hybrisProjectDescriptor.getModulesFilesDirectory();
        if (modulesFilesDirectory != null && !modulesFilesDirectory.exists()) {
            modulesFilesDirectory.mkdirs();
        }

        this.initializeHybrisProjectSettings(projectSettings);
        this.updateProjectDictionary(project, hybrisProjectDescriptor.getModulesChosenForImport());
        this.selectSdk(project);

        if (!refresh) {
            this.saveCustomDirectoryLocation(project, projectSettings);
            projectSettings.setExcludedFromScanning(hybrisProjectDescriptor.getExcludedFromScanning());
        }

        this.saveImportedSettings(projectSettings, appSettings, projectSettings);
        this.disableWrapOnType(ImpExLanguage.INSTANCE);

        processUltimateEdition(indicator);

        ModifiableModuleModel rootProjectModifiableModel = model == null
            ? modifiableModelsProvider.getModifiableModuleModel()
            : model;

        configuratorFactory.getPreImportConfigurators().forEach(configurator ->
            configurator.preConfigure(indicator, hybrisProjectDescriptor, allHybrisModuleDescriptors)
        );

        int counter = 0;

        final var application = ApplicationManager.getApplication();

        for (final var moduleDescriptor : allHybrisModules) {
            final var javaModule = createJavaModule(indicator, allYModules, rootProjectModifiableModel, moduleDescriptor, appSettings);
            modules.add(javaModule);
            counter++;

            if (counter >= COMMITTED_CHUNK_SIZE) {
                counter = 0;
                application.invokeAndWait(() -> application.runWriteAction(modifiableModelsProvider::commit));

                modifiableModelsProvider = new IdeModifiableModelsProviderImpl(project);

                rootProjectModifiableModel = model == null
                    ? modifiableModelsProvider.getModifiableModuleModel()
                    : model;
            }
        }

        final var finalRootProjectModifiableModel = rootProjectModifiableModel;
        configuratorFactory.getImportConfigurators().forEach(configurator ->
            configurator.configure(project, indicator, hybrisProjectDescriptor, allHybrisModuleDescriptors, finalRootProjectModifiableModel, modifiableModelsProvider, cache)
        );

        indicator.setText(message("hybris.project.import.saving.project"));

        application.invokeAndWait(() -> application.runWriteAction(modifiableModelsProvider::commit));

        configureJavaCompiler(indicator, cache);
        configureAngularModules(indicator, appSettings);

        project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, Boolean.TRUE);
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

        configuratorFactory.getFacetConfigurators().forEach(configurator ->
            configurator.configureModuleFacet(module, hybrisProjectDescriptor, modifiableFacetModel, moduleDescriptor, modifiableRootModel)
        );
    }

    // TODO: double check CCv2 modules handling
    private List<ModuleDescriptor> getHybrisModuleDescriptors() {
        return hybrisProjectDescriptor.getModulesChosenForImport().stream()
            .filter(e -> !(e instanceof ExternalModuleDescriptor))
            .toList();
    }

    private void configureJavaCompiler(final @NotNull ProgressIndicator indicator, final ConfiguratorCache cache) {
        final JavaCompilerConfigurator compilerConfigurator = configuratorFactory.getJavaCompilerConfigurator();

        if (compilerConfigurator == null) return;

        indicator.setText(message("hybris.project.import.compiler.java"));
        compilerConfigurator.configure(hybrisProjectDescriptor, project, cache);
    }

    @Deprecated(since = "Migrate to EP")
    private void configureAngularModules(
        final @NotNull ProgressIndicator indicator,
        final ApplicationSettings appSettings
    ) {
        if (Plugin.ANGULAR.isDisabled()) return;

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
            .peek(descriptor -> {
                final var applicationSettings = ApplicationSettings.getInstance();
                if (applicationSettings.getGroupModules()) {
                    final var predefinedGroups = ModuleGroupingUtil.getPredefinedGroups(applicationSettings);
                    final var groupNames = ModuleGroupingUtil.getGroupName(descriptor, descriptor.getDirectDependencies(), predefinedGroups);

                    if (groupNames != null) {
                        descriptor.setGroupNames(groupNames);
                    }
                }
            })
            .collect(Collectors.toMap(Function.identity(), module -> rootProjectModifiableModel.newModule(
                module.ideaModuleFile().getAbsolutePath(),
                StdModuleTypes.JAVA.getId()
            )));

        modules.forEach((descriptor, module) -> {
            final var modifiableRootModel = modifiableModelsProvider.getModifiableRootModel(module);

            contentRootConfigurator.configure(indicator, modifiableRootModel, descriptor, appSettings);
            configureModuleFacet(descriptor, module, modifiableRootModel, modifiableModelsProvider);
        });

        final var application = ApplicationManager.getApplication();
        application.invokeAndWait(() -> application.runWriteAction(modifiableModelsProvider::commit));

//        configurator.configure(project, modules.values());
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

    private void initializeHybrisProjectSettings(final @NotNull ProjectSettings projectSettings) {
        WorkspaceSettings.getInstance(project).setHybrisProject(true);
        final var plugin = Plugin.HYBRIS.getPluginDescriptor();

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
                                      @NotNull final ProjectSettings hybrisSettingsComponent) {
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

        final var credentialAttributes = new CredentialAttributes(CCv2Constants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN);
        PasswordSafe.getInstance().setPassword(credentialAttributes, hybrisProjectDescriptor.getCcv2Token());

        projectSettings.setJavadocUrl(hybrisProjectDescriptor.getJavadocUrl());
        final var completeSetOfHybrisModules = hybrisProjectDescriptor.getFoundModules().stream()
            .filter(e -> !(e instanceof ExternalModuleDescriptor)
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
