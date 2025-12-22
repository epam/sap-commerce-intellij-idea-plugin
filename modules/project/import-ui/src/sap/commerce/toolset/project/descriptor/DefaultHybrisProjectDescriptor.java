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

package sap.commerce.toolset.project.descriptor;

import com.google.common.collect.Sets;
import com.intellij.execution.wsl.WSLDistribution;
import com.intellij.execution.wsl.WslDistributionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.ccv2.CCv2Constants;
import sap.commerce.toolset.exceptions.HybrisConfigurationException;
import sap.commerce.toolset.project.HybrisProjectImportService;
import sap.commerce.toolset.project.HybrisProjectService;
import sap.commerce.toolset.project.ProjectConstants;
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor;
import sap.commerce.toolset.project.descriptor.impl.YHmcSubModuleDescriptor;
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor;
import sap.commerce.toolset.project.factories.ModuleDescriptorFactory;
import sap.commerce.toolset.project.localextensions.ProjectImportLocalExtensionsProcessor;
import sap.commerce.toolset.project.tasks.TaskProgressProcessor;
import sap.commerce.toolset.project.utils.FileUtils;
import sap.commerce.toolset.settings.ApplicationSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sap.commerce.toolset.HybrisI18nBundle.message;

public class DefaultHybrisProjectDescriptor implements HybrisProjectDescriptor {

    private static final Logger LOG = Logger.getInstance(DefaultHybrisProjectDescriptor.class);
    @NotNull
    protected final List<ModuleDescriptor> foundModules = new ArrayList<>();
    @NotNull
    protected final List<ModuleDescriptor> modulesChosenForImport = new ArrayList<>();
    private final Set<File> vcs = new HashSet<>();
    private final Set<String> excludedFromScanning = new HashSet<>();
    @Nullable
    protected Project project;
    private final boolean refresh;
    private boolean openProjectSettingsAfterImport;
    private final File rootDirectory;
    @Nullable
    protected File modulesFilesDirectory;
    @Nullable
    protected String ccv2Token;
    @Nullable
    protected File sourceCodeFile;
    @Nullable
    protected File projectIconFile;
    // TODO: why nullable, it is mandatory, without it we should not create this class
    @Nullable
    protected File hybrisDistributionDirectory;
    @Nullable
    protected File externalExtensionsDirectory;
    @Nullable
    protected File externalConfigDirectory;
    @Nullable
    protected File externalDbDriversDirectory;
    @Nullable
    protected String javadocUrl;
    @Nullable
    protected String hybrisVersion;
    private final ProjectImportSettings importContext;

    @NotNull
    private ConfigModuleDescriptor configHybrisModuleDescriptor;
    @NotNull
    private PlatformModuleDescriptor platformHybrisModuleDescriptor;
    @Nullable
    private ModuleDescriptor kotlinNatureModuleDescriptor;

    public DefaultHybrisProjectDescriptor(
        final @NotNull File rootDirectory,
        final @NotNull ProjectImportSettings importContext,
        final boolean refresh,
        final @Nullable Project project
    ) {
        this.rootDirectory = rootDirectory;
        this.refresh = refresh;
        this.importContext = importContext;
        this.project = project;
    }

    protected ArrayList<ModuleDescriptor> scanDirectoryForHybrisModules(
        @NotNull final File rootDirectory,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor,
        @Nullable final TaskProgressProcessor<List<File>> errorsProcessor
    ) throws InterruptedException, IOException {

        this.foundModules.clear();

        final var moduleRootMap = newModuleRootMap();
        final var excludedFromScanning = getExcludedFromScanningDirectories(rootDirectory);

        LOG.info("Scanning for modules");
        findModuleRoots(rootDirectory, moduleRootMap, excludedFromScanning, false, rootDirectory, progressListenerProcessor);

        if (externalExtensionsDirectory != null && !FileUtils.isFileUnder(externalExtensionsDirectory, rootDirectory)) {
            LOG.info("Scanning for external modules");
            findModuleRoots(rootDirectory, moduleRootMap, excludedFromScanning, false, externalExtensionsDirectory, progressListenerProcessor);
        }

        if (hybrisDistributionDirectory != null && !FileUtils.isFileUnder(hybrisDistributionDirectory, rootDirectory)) {
            LOG.info("Scanning for hybris modules out of the project");
            findModuleRoots(rootDirectory, moduleRootMap, excludedFromScanning, false, hybrisDistributionDirectory, progressListenerProcessor);
        }
        final var moduleRootDirectories = processDirectoriesByTypePriority(
            rootDirectory,
            moduleRootMap,
            excludedFromScanning,
            importContext.getScanThroughExternalModule(),
            progressListenerProcessor
        );

        final var moduleDescriptors = new ArrayList<ModuleDescriptor>();
        final var pathsFailedToImport = new ArrayList<File>();

        addRootModule(rootDirectory, moduleDescriptors, pathsFailedToImport, ApplicationSettings.getInstance().getGroupModules());

        for (final var moduleRootDirectory : moduleRootDirectories) {
            try {
                final var moduleDescriptor = ModuleDescriptorFactory.INSTANCE.createDescriptor(moduleRootDirectory, this);
                moduleDescriptors.add(moduleDescriptor);

                if (moduleDescriptor instanceof final YModuleDescriptor yModuleDescriptor) {
                    moduleDescriptors.addAll(yModuleDescriptor.getSubModules());
                }
            } catch (HybrisConfigurationException e) {
                LOG.error("Can not import a module using path: " + pathsFailedToImport, e);

                pathsFailedToImport.add(moduleRootDirectory);
            }
        }

        if (moduleDescriptors.stream().noneMatch(PlatformModuleDescriptor.class::isInstance)) {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
                message("hybris.project.import.scan.platform.not.found"),
                message("hybris.project.error")
            ));

            throw new InterruptedException("Unable to find Platform module.");
        }

        if (null != errorsProcessor) {
            if (errorsProcessor.shouldContinue(pathsFailedToImport)) {
                throw new InterruptedException("Modules scanning has been interrupted.");
            }
        }

        Collections.sort(moduleDescriptors);

        buildDependencies(moduleDescriptors);
        final var addons = processAddons(moduleDescriptors);
        removeNotInstalledAddons(moduleDescriptors, addons);
        removeHmcSubModules(moduleDescriptors);

        return moduleDescriptors;
    }

    @Override
    public void setExcludedFromScanning(final Set<String> excludedFromScanning) {
        this.excludedFromScanning.clear();
        this.excludedFromScanning.addAll(excludedFromScanning);
    }

    @Override
    public Set<String> getExcludedFromScanning() {
        return Collections.unmodifiableSet(excludedFromScanning);
    }

    private Set<File> getExcludedFromScanningDirectories(final @NotNull File rootDirectory) {
        return this.excludedFromScanning.stream()
            .map(it -> new File(rootDirectory, it))
            .filter(File::exists)
            .filter(File::isDirectory)
            .collect(Collectors.toSet());
    }

    private List<YAcceleratorAddonSubModuleDescriptor> processAddons(final List<ModuleDescriptor> moduleDescriptors) {
        final var addons = moduleDescriptors.stream()
            .filter(YAcceleratorAddonSubModuleDescriptor.class::isInstance)
            .map(YAcceleratorAddonSubModuleDescriptor.class::cast)
            .toList();

        for (final var module : moduleDescriptors) {
            if (module instanceof final YModuleDescriptor yModule) {
                for (final var yAddon : addons) {
                    if (!yModule.equals(yAddon) && yModule.getDirectDependencies().contains(yAddon.getOwner())) {
                        yAddon.addTargetModule(yModule);
                    }
                }
            }
        }

        // update direct dependencies for addons
        addons.stream()
            .filter(Predicate.not(it -> it.getTargetModules().isEmpty()))
            .forEach(it -> {
                final var targetModules = it.getTargetModules().stream()
                    .map(YModuleDescriptor::getSubModules)
                    .flatMap(Collection::stream)
                    .filter(YWebSubModuleDescriptor.class::isInstance)
                    .map(YWebSubModuleDescriptor.class::cast)
                    .collect(Collectors.toSet());
                it.addRequiredExtensionNames(targetModules);
                it.addDirectDependencies(targetModules);
            });

        return addons;
    }

    private void removeHmcSubModules(final List<ModuleDescriptor> moduleDescriptors) {
        final var hmcSubModules = new HashMap<YModuleDescriptor, YHmcSubModuleDescriptor>();
        ModuleDescriptor hmcModule = null;

        for (final var module : moduleDescriptors) {
            if (ProjectConstants.Extension.HMC.equals(module.getName())) {
                hmcModule = module;
            }
            if (module instanceof final YModuleDescriptor yModule) {
                yModule.getSubModules().stream()
                    .filter(YHmcSubModuleDescriptor.class::isInstance)
                    .map(YHmcSubModuleDescriptor.class::cast)
                    .findAny()
                    .ifPresent(hmcSubModule -> hmcSubModules.put(yModule, hmcSubModule));

            }
        }

        if (hmcModule == null) {
            hmcSubModules.forEach(YModuleDescriptor::removeSubModule);
            moduleDescriptors.removeAll(hmcSubModules.values());
        }
    }

    private void removeNotInstalledAddons(
        final List<ModuleDescriptor> moduleDescriptors,
        final List<YAcceleratorAddonSubModuleDescriptor> addons
    ) {
        final var notInstalledAddons = addons.stream()
            .filter(it -> it.getTargetModules().isEmpty())
            .toList();

        notInstalledAddons.forEach(it -> it.getOwner().removeSubModule(it));
        moduleDescriptors.removeAll(notInstalledAddons);
    }

    // scan through eclipse module for hybris custom mudules in its subdirectories
    private Set<File> processDirectoriesByTypePriority(
        final @NotNull File rootDirectory, @NotNull final Map<DescriptorDirectoryType, Set<File>> moduleRootMap,
        final Set<File> excludedFromScanning,
        final boolean scanThroughExternalModule,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor
    ) throws InterruptedException, IOException {
        final Map<String, File> moduleRootDirectories = new HashMap<>();

        moduleRootMap.get(DescriptorDirectoryType.HYBRIS).forEach(file -> addIfNotExists(rootDirectory, moduleRootDirectories, file));

        if (scanThroughExternalModule) {
            LOG.info("Scanning for higher priority modules");
            for (final File nonHybrisDir : moduleRootMap.get(DescriptorDirectoryType.NON_HYBRIS)) {
                final Map<DescriptorDirectoryType, Set<File>> nonHybrisModuleRootMap = newModuleRootMap();
                scanForSubdirectories(nonHybrisModuleRootMap, excludedFromScanning, true, nonHybrisDir.toPath(), progressListenerProcessor);
                final Set<File> hybrisModuleSet = nonHybrisModuleRootMap.get(DescriptorDirectoryType.HYBRIS);
                if (hybrisModuleSet.isEmpty()) {
                    LOG.info("Confirmed module " + nonHybrisDir);
                    addIfNotExists(rootDirectory, moduleRootDirectories, nonHybrisDir);
                } else {
                    LOG.info("Replaced module " + nonHybrisDir);
                    hybrisModuleSet.forEach(file -> addIfNotExists(rootDirectory, moduleRootDirectories, file));
                }
            }
        } else {
            moduleRootMap.get(DescriptorDirectoryType.NON_HYBRIS).forEach(file -> addIfNotExists(rootDirectory, moduleRootDirectories, file));
        }

        moduleRootMap.get(DescriptorDirectoryType.CCV2).forEach(file -> addIfNotExists(rootDirectory, moduleRootDirectories, file));

        return Sets.newHashSet(moduleRootDirectories.values());
    }

    private void addIfNotExists(final @NotNull File rootDirectory, final Map<String, File> moduleRootDirectories, final File file) {
        try {
            // this will resolve symlinks
            final String path = file.getCanonicalPath();
            final File current = moduleRootDirectories.get(path);
            if (current == null) {
                moduleRootDirectories.put(path, file);
                return;
            }
            if (hybrisDistributionDirectory != null && !FileUtils.isFileUnder(current, hybrisDistributionDirectory)) {
                if (FileUtils.isFileUnder(file, hybrisDistributionDirectory)) {
                    moduleRootDirectories.put(path, file);
                    return;
                }
            }
            if (externalExtensionsDirectory != null && !FileUtils.isFileUnder(current, externalExtensionsDirectory)) {
                if (FileUtils.isFileUnder(file, externalExtensionsDirectory)) {
                    moduleRootDirectories.put(path, file);
                    return;
                }
            }
            if (rootDirectory != null && !FileUtils.isFileUnder(current, rootDirectory)) {
                if (FileUtils.isFileUnder(file, rootDirectory)) {
                    moduleRootDirectories.put(path, file);
                }
            }
        } catch (IOException e) {
            LOG.error("Unable to locate " + file.getAbsolutePath());
        }
    }

    private Map<DescriptorDirectoryType, Set<File>> newModuleRootMap() {
        return Map.of(
            DescriptorDirectoryType.HYBRIS, new HashSet<>(),
            DescriptorDirectoryType.NON_HYBRIS, new HashSet<>(),
            DescriptorDirectoryType.CCV2, new HashSet<>()
        );
    }

    private void addRootModule(
        final File rootDirectory, final List<ModuleDescriptor> moduleDescriptors,
        final List<File> pathsFailedToImport,
        final boolean groupModules
    ) {
        if (groupModules) {
            return;
        }

        try {
            final var rootDescriptor = ModuleDescriptorFactory.INSTANCE.createRootDescriptor(rootDirectory, this, rootDirectory.getName());
            moduleDescriptors.add(rootDescriptor);
        } catch (HybrisConfigurationException e) {
            LOG.error("Can not import a module using path: " + pathsFailedToImport, e);
            pathsFailedToImport.add(rootDirectory);
        }
    }

    @Override
    public void setHybrisProject(@Nullable final Project project) {
        this.project = project;
    }

    private void findModuleRoots(
        final @NotNull File rootDirectory, @NotNull final Map<DescriptorDirectoryType, Set<File>> moduleRootMap,
        final Set<File> excludedFromScanning,
        final boolean acceptOnlyHybrisModules,
        @NotNull final File rootProjectDirectory,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor
    ) throws InterruptedException, IOException {
        if (null != progressListenerProcessor) {
            if (!progressListenerProcessor.shouldContinue(rootProjectDirectory)) {
                LOG.error("Modules scanning has been interrupted.");
                throw new InterruptedException("Modules scanning has been interrupted.");
            }
        }

        if (rootProjectDirectory.isHidden()) {
            LOG.debug("Skipping hidden directory: ", rootProjectDirectory);
            return;
        }
        if (excludedFromScanning.contains(rootProjectDirectory)) {
            LOG.debug("Skipping excluded directory: ", rootProjectDirectory);
            return;
        }

        final HybrisProjectService hybrisProjectService = ApplicationManager.getApplication().getService(HybrisProjectService.class);

        if (hybrisProjectService.hasVCS(rootProjectDirectory)) {
            LOG.info("Detected version control service " + rootProjectDirectory.getAbsolutePath());
            vcs.add(rootProjectDirectory.getCanonicalFile());
        }

        if (hybrisProjectService.isHybrisModule(rootProjectDirectory)) {
            LOG.info("Detected hybris module " + rootProjectDirectory.getAbsolutePath());
            moduleRootMap.get(DescriptorDirectoryType.HYBRIS).add(rootProjectDirectory);
            return;
        }
        if (hybrisProjectService.isConfigModule(rootProjectDirectory)) {
            LOG.info("Detected config module " + rootProjectDirectory.getAbsolutePath());
            moduleRootMap.get(DescriptorDirectoryType.HYBRIS).add(rootProjectDirectory);
            return;
        }

        if (!acceptOnlyHybrisModules) {
            // TODO: review this logic
            if (!rootProjectDirectory.getAbsolutePath().endsWith(HybrisConstants.PLATFORM_MODULE)
                && !FileUtil.filesEqual(rootProjectDirectory, rootDirectory)
                && (hybrisProjectService.isGradleModule(rootProjectDirectory) || hybrisProjectService.isGradleKtsModule(rootProjectDirectory))
                && !hybrisProjectService.isCCv2Module(rootProjectDirectory)) {

                LOG.info("Detected gradle module " + rootProjectDirectory.getAbsolutePath());

                moduleRootMap.get(DescriptorDirectoryType.NON_HYBRIS).add(rootProjectDirectory);
            }

            if (hybrisProjectService.isMavenModule(rootProjectDirectory)
                && !FileUtil.filesEqual(rootProjectDirectory, rootDirectory)
                && !hybrisProjectService.isCCv2Module(rootProjectDirectory)
            ) {
                LOG.info("Detected maven module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(DescriptorDirectoryType.NON_HYBRIS).add(rootProjectDirectory);
            }

            if (hybrisProjectService.isPlatformModule(rootProjectDirectory)) {
                LOG.info("Detected platform module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(DescriptorDirectoryType.HYBRIS).add(rootProjectDirectory);
            } else if (hybrisProjectService.isEclipseModule(rootProjectDirectory)
                && !FileUtil.filesEqual(rootProjectDirectory, rootDirectory)
            ) {
                LOG.info("Detected eclipse module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(DescriptorDirectoryType.NON_HYBRIS).add(rootProjectDirectory);
            }

            if (hybrisProjectService.isCCv2Module(rootProjectDirectory)) {
                LOG.info("Detected CCv2 module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(DescriptorDirectoryType.CCV2).add(rootProjectDirectory);
                final var name = rootProjectDirectory.getName();
                if (name.endsWith(CCv2Constants.DATAHUB_NAME)) {
                    // faster import: no need to process sub-folders of the CCv2 js-storefront and datahub directories
                    return;
                }
            }

            if (hybrisProjectService.isAngularModule(rootProjectDirectory)) {
                LOG.info("Detected Angular module " + rootProjectDirectory.getAbsolutePath());
                moduleRootMap.get(DescriptorDirectoryType.NON_HYBRIS).add(rootProjectDirectory);
                // do not go deeper
                return;
            }
        }

        scanForSubdirectories(moduleRootMap, excludedFromScanning, acceptOnlyHybrisModules, rootProjectDirectory.toPath(), progressListenerProcessor);
    }

    private void scanForSubdirectories(
        final Map<DescriptorDirectoryType, Set<File>> moduleRootMap,
        final Set<File> excludedFromScanning,
        final boolean acceptOnlyHybrisModules,
        final Path rootProjectDirectory,
        final TaskProgressProcessor<File> progressListenerProcessor
    ) throws IOException, InterruptedException {
        if (!Files.isDirectory(rootProjectDirectory)) return;

        if (isPathInWSLDistribution(rootProjectDirectory)) {
            scanSubdirectoriesWSL(moduleRootMap, excludedFromScanning, acceptOnlyHybrisModules, rootProjectDirectory, progressListenerProcessor);
        } else {
            scanSubdirectories(moduleRootMap, excludedFromScanning, acceptOnlyHybrisModules, rootProjectDirectory, progressListenerProcessor);
        }
    }

    private void scanSubdirectories(
        @NotNull final Map<DescriptorDirectoryType, Set<File>> moduleRootMap,
        final Set<File> excludedFromScanning,
        final boolean acceptOnlyHybrisModules,
        @NotNull final Path rootProjectDirectory,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor
    ) throws InterruptedException, IOException {
        final var importService = HybrisProjectImportService.getInstance();

        final DirectoryStream<Path> files = Files.newDirectoryStream(rootProjectDirectory, file -> {
            if (file == null) {
                return false;
            }
            if (!Files.isDirectory(file)) {
                return false;
            }
            if (importService.isDirectoryExcluded(file)) {
                return false;
            }
            return !Files.isSymbolicLink(file) || importContext.getFollowSymlink();
        });
        if (files != null) {
            for (final var file : files) {
                findModuleRoots(rootDirectory, moduleRootMap, excludedFromScanning, acceptOnlyHybrisModules, file.toFile(), progressListenerProcessor);
            }
            files.close();
        }
    }

    private void scanSubdirectoriesWSL(
        @NotNull final Map<DescriptorDirectoryType, Set<File>> moduleRootMap,
        final Set<File> excludedFromScanning,
        final boolean acceptOnlyHybrisModules,
        @NotNull final Path rootProjectDirectory,
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor
    ) throws InterruptedException, IOException {
        final var importService = HybrisProjectImportService.getInstance();

        try (final Stream<Path> stream = Files.list(rootProjectDirectory)) {
            final var moduleRoots = stream
                .filter(Objects::nonNull)
                .filter(Files::isDirectory)
                .filter(Predicate.not(importService::isDirectoryExcluded))
                .filter(file -> !Files.isSymbolicLink(file) || importContext.getFollowSymlink())
                .map(Path::toFile)
                .toList();
            for (final var moduleRoot : moduleRoots) {
                findModuleRoots(rootDirectory, moduleRootMap, excludedFromScanning, acceptOnlyHybrisModules, moduleRoot, progressListenerProcessor);
            }
        }
    }

    public static boolean isPathInWSLDistribution(@NotNull final Path rootProjectDirectory) {
        return WslDistributionManager.getInstance().getInstalledDistributions().stream()
            .map(WSLDistribution::getUNCRootPath)
            .map(String::valueOf)
            .filter(StringUtils::isNoneBlank)
            .anyMatch(wslRoot -> rootProjectDirectory.toString().startsWith(wslRoot));
    }

    protected void buildDependencies(@NotNull final Collection<ModuleDescriptor> moduleDescriptors) {
        final var moduleDescriptorsMap = moduleDescriptors.stream()
            .filter(distinctByKey(ModuleDescriptor::getName))
            .collect(Collectors.toMap(ModuleDescriptor::getName, Function.identity()));
        for (final var moduleDescriptor : moduleDescriptors) {
            final var dependencies = buildDependencies(moduleDescriptor, moduleDescriptorsMap);
            moduleDescriptor.addDirectDependencies(dependencies);
        }
    }

    public static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private Set<ModuleDescriptor> buildDependencies(final ModuleDescriptor moduleDescriptor, final Map<String, ModuleDescriptor> moduleDescriptors) {
        moduleDescriptor.computeRequiredExtensionNames(moduleDescriptors);

        var requiredExtensionNames = moduleDescriptor.getRequiredExtensionNames();

        if (CollectionUtils.isEmpty(requiredExtensionNames)) {
            return Collections.emptySet();
        }
        requiredExtensionNames = requiredExtensionNames.stream()
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));

        final var dependencies = new LinkedHashSet<ModuleDescriptor>(requiredExtensionNames.size());

        for (String requiresExtensionName : requiredExtensionNames) {
            final ModuleDescriptor dependsOn = moduleDescriptors.get(requiresExtensionName);

            if (dependsOn == null) {
                // TODO: possible case due optional sub-modules, xxx.web | xxx.backoffice | etc.
                LOG.trace(String.format(
                    "Module '%s' contains unsatisfied dependency '%s'.",
                    moduleDescriptor.getName(), requiresExtensionName
                ));
            } else {
                dependencies.add(dependsOn);
            }
        }

        return dependencies;
    }

    @Nullable
    @Override
    public Project getProject() {
        return this.project;
    }

    @Override
    public boolean getRefresh() {
        return refresh;
    }

    @NotNull
    @Override
    public List<ModuleDescriptor> getFoundModules() {
        return Collections.unmodifiableList(this.foundModules);
    }

    @NotNull
    @Override
    public List<ModuleDescriptor> getChosenModuleDescriptors() {
        return this.modulesChosenForImport;
    }

    @Nullable
    @Override
    public ConfigModuleDescriptor getConfigHybrisModuleDescriptor() {
        return configHybrisModuleDescriptor;
    }

    @NotNull
    @Override
    public PlatformModuleDescriptor getPlatformHybrisModuleDescriptor() {
        return platformHybrisModuleDescriptor;
    }

    @Nullable
    @Override
    public ModuleDescriptor getKotlinNatureModuleDescriptor() {
        return kotlinNatureModuleDescriptor;
    }

    @Nullable
    @Override
    public File getRootDirectory() {
        return this.rootDirectory;
    }

    @Override
    public void clear() {
        this.hybrisDistributionDirectory = null;
        this.externalExtensionsDirectory = null;
        this.externalConfigDirectory = null;
        this.externalDbDriversDirectory = null;
        this.foundModules.clear();
        this.modulesChosenForImport.clear();
        this.vcs.clear();
    }

    @Nullable
    @Override
    public File getModulesFilesDirectory() {
        return this.modulesFilesDirectory;
    }

    @Override
    public void setModulesFilesDirectory(@Nullable final File modulesFilesDirectory) {
        this.modulesFilesDirectory = modulesFilesDirectory;
    }

    @Override
    public @Nullable String getCcv2Token() {
        return ccv2Token;
    }

    @Override
    public void setCcv2Token(@Nullable final String ccv2Token) {
        this.ccv2Token = ccv2Token;
    }

    @Nullable
    @Override
    public File getSourceCodeFile() {
        return sourceCodeFile;
    }

    @Override
    public void setSourceCodeFile(@Nullable final File sourceCodeFile) {
        this.sourceCodeFile = sourceCodeFile;
    }

    @Override
    public @Nullable File getProjectIconFile() {
        return projectIconFile;
    }

    @Override
    public boolean getOpenProjectSettingsAfterImport() {
        return openProjectSettingsAfterImport;
    }

    @Override
    public void setOpenProjectSettingsAfterImport(final boolean openProjectSettingsAfterImport) {
        this.openProjectSettingsAfterImport = openProjectSettingsAfterImport;
    }

    @Override
    public void setProjectIconFile(final File projectIconFile) {
        this.projectIconFile = projectIconFile;
    }

    @Override
    public String getHybrisVersion() {
        return hybrisVersion;
    }

    @Override
    public void setHybrisVersion(final String hybrisVersion) {
        this.hybrisVersion = hybrisVersion;
    }

    @Override
    public void setRootDirectoryAndScanForModules(
        @Nullable final TaskProgressProcessor<File> progressListenerProcessor,
        @Nullable final TaskProgressProcessor<List<File>> errorsProcessor
    ) {
        try {
            final ArrayList<ModuleDescriptor> moduleDescriptors = this.scanDirectoryForHybrisModules(rootDirectory, progressListenerProcessor, errorsProcessor);
            foundModules.addAll(moduleDescriptors);

            ProjectImportLocalExtensionsProcessor.Companion.getInstance().processLocalExtensions(this);
        } catch (InterruptedException | IOException e) {
            LOG.warn(e);

            this.foundModules.clear();
        }
    }

    @Override
    @Nullable
    public File getHybrisDistributionDirectory() {
        return hybrisDistributionDirectory;
    }

    @Override
    public void setHybrisDistributionDirectory(@Nullable final File hybrisDistributionDirectory) {
        this.hybrisDistributionDirectory = hybrisDistributionDirectory;
    }

    @Override
    @Nullable
    public File getExternalExtensionsDirectory() {
        return externalExtensionsDirectory;
    }

    @Override
    public void setExternalExtensionsDirectory(@Nullable final File externalExtensionsDirectory) {
        this.externalExtensionsDirectory = externalExtensionsDirectory;
    }

    @Override
    @Nullable
    public File getExternalConfigDirectory() {
        return externalConfigDirectory;
    }

    @Override
    public void setExternalConfigDirectory(@Nullable final File externalConfigDirectory) {
        this.externalConfigDirectory = externalConfigDirectory;
    }

    @Override
    @Nullable
    public File getExternalDbDriversDirectory() {
        return externalDbDriversDirectory;
    }

    @Override
    public void setExternalDbDriversDirectory(@Nullable final File externalDbDriversDirectory) {
        this.externalDbDriversDirectory = externalDbDriversDirectory;
    }

    @Nullable
    @Override
    public String getJavadocUrl() {
        return javadocUrl;
    }

    @Override
    public void setJavadocUrl(@Nullable final String javadocUrl) {
        this.javadocUrl = javadocUrl;
    }

    @Override
    public Set<File> getDetectedVcs() {
        return vcs;
    }

    @Override
    public String toString() {
        return "DefaultHybrisProjectDescriptor{" +
            "rootDirectory=" + rootDirectory +
            ", modulesFilesDirectory=" + modulesFilesDirectory +
            ", sourceCodeFile=" + sourceCodeFile +
            ", hybrisDistributionDirectory=" + hybrisDistributionDirectory +
            ", externalExtensionsDirectory=" + externalExtensionsDirectory +
            ", externalConfigDirectory=" + externalConfigDirectory +
            ", externalDbDriversDirectory=" + externalDbDriversDirectory +
            ", javadocUrl='" + javadocUrl + '\'' +
            ", hybrisVersion='" + hybrisVersion + '\'' +
            ", importSettings=" + importContext +
            '}';
    }

    @Override
    public @NotNull ProjectImportSettings getImportContext() {
        return importContext;
    }

    @Override
    public void setConfigHybrisModuleDescriptor(@Nullable final ConfigModuleDescriptor configModuleDescriptor) {
        this.configHybrisModuleDescriptor = configModuleDescriptor;
    }

    @Override
    public void setPlatformHybrisModuleDescriptor(@NotNull final PlatformModuleDescriptor platformModuleDescriptor) {
        this.platformHybrisModuleDescriptor = platformModuleDescriptor;
    }

    @Override
    public void setKotlinNatureModuleDescriptor(@Nullable final ModuleDescriptor moduleDescriptor) {
        this.kotlinNatureModuleDescriptor = moduleDescriptor;
    }
}
