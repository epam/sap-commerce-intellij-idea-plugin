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

package sap.commerce.toolset.project;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.HybrisIcons;
import sap.commerce.toolset.Notifications;
import sap.commerce.toolset.project.configurator.PostImportBulkConfigurator;
import sap.commerce.toolset.project.descriptor.*;
import sap.commerce.toolset.project.descriptor.impl.ExternalModuleDescriptor;
import sap.commerce.toolset.project.settings.ProjectSettings;
import sap.commerce.toolset.project.tasks.ImportProjectProgressModalWindow;
import sap.commerce.toolset.project.tasks.SearchModulesRootsTaskModalWindow;
import sap.commerce.toolset.project.vfs.VirtualFileSystemService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static sap.commerce.toolset.HybrisI18NBundleUtils.message;
import static sap.commerce.toolset.project.descriptor.ModuleDescriptorImportStatus.MANDATORY;
import static sap.commerce.toolset.project.descriptor.ModuleDescriptorImportStatus.UNUSED;

public class DefaultHybrisProjectImportBuilder extends ProjectImportBuilder<ModuleDescriptor> implements HybrisProjectImportBuilder {

    private static final Logger LOG = Logger.getInstance(DefaultHybrisProjectImportBuilder.class);

    protected final Lock lock = new ReentrantLock();

    @Nullable
    private volatile HybrisProjectDescriptor hybrisProjectDescriptor;
    private List<ModuleDescriptor> moduleList;
    private List<ModuleDescriptor> _hybrisModulesToImport;

    @Override
    @Nullable
    public Project createProject(@NotNull final String name, @NotNull final String path) {
        final Project project = super.createProject(name, path);
        getHybrisProjectDescriptor().setHybrisProject(project);
        return project;
    }

    @Override
    public void setRootProjectDirectory(@NotNull final File directory) {
        LOG.info("setting RootProjectDirectory to " + directory.getAbsolutePath());
        ProgressManager.getInstance().run(new SearchModulesRootsTaskModalWindow(
            directory, this.getHybrisProjectDescriptor()
        ));

        this.setFileToImport(directory.getAbsolutePath());
    }

    @Override
    public void cleanup() {
        super.cleanup();

        try {
            this.lock.lock();
            if (this.hybrisProjectDescriptor != null) {
                this.hybrisProjectDescriptor.clear();
                this.hybrisProjectDescriptor = null;
            }
        } finally {
            this.lock.unlock();
        }
    }

    @NotNull
    @Override
    public HybrisProjectDescriptor getHybrisProjectDescriptor() {
        try {
            this.lock.lock();

            if (null == this.hybrisProjectDescriptor) {
                this.hybrisProjectDescriptor = new DefaultHybrisProjectDescriptor();
                this.hybrisProjectDescriptor.setRefresh(isUpdate());
                this.hybrisProjectDescriptor.setProject(getCurrentProject());
            }

            return this.hybrisProjectDescriptor;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean isOpenProjectSettingsAfter() {
        return this.getHybrisProjectDescriptor().isOpenProjectSettingsAfterImport();
    }

    @Override
    public void setOpenProjectSettingsAfter(final boolean on) {
        this.getHybrisProjectDescriptor().setOpenProjectSettingsAfterImport(on);
    }

    @Nullable
    @Override
    public List<Module> commit(
        final Project project,
        @Nullable final ModifiableModuleModel model,
        final ModulesProvider modulesProvider,
        final ModifiableArtifactModel artifactModel
    ) {
        final var hybrisProjectDescriptor = getHybrisProjectDescriptor();
        final var allModules = hybrisProjectDescriptor.getChosenModuleDescriptors();

        if (allModules.isEmpty()) return Collections.emptyList();

        this.performProjectsCleanup(allModules);

        final var modules = new ArrayList<Module>();
        new ImportProjectProgressModalWindow(project, hybrisProjectDescriptor, modules)
            .queue();

        if (isUpdate()) {
            PostImportBulkConfigurator.getInstance(project).configure(hybrisProjectDescriptor);
        } else {
            project.putUserData(ProjectConstants.getKEY_FINALIZE_PROJECT_IMPORT(), hybrisProjectDescriptor);
        }
        notifyImportNotFinishedYet(project);
        return modules;
    }

    private void notifyImportNotFinishedYet(@NotNull final Project project) {

        final String notificationTitle = isUpdate()
            ? message("hybris.notification.project.refresh.title")
            : message("hybris.notification.project.import.title");

        Notifications.create(NotificationType.INFORMATION, notificationTitle,
                message("hybris.notification.import.or.refresh.process.not.finished.yet.content"))
            .notify(project);
    }

    @Deprecated(since = "Compare to refresh action, looks like we may not remove existing modules in case of configured IML files.")
    protected void performProjectsCleanup(@NotNull final Iterable<ModuleDescriptor> modulesChosenForImport) {
        final List<File> alreadyExistingModuleFiles;
        final File dir = hybrisProjectDescriptor.getModulesFilesDirectory();
        if (dir != null && dir.isDirectory()) {
            alreadyExistingModuleFiles = getAllImlFiles(dir);
        } else {
            alreadyExistingModuleFiles = getModulesChosenForImportFiles(modulesChosenForImport);
        }
        Collections.sort(alreadyExistingModuleFiles);

        try {
            VirtualFileSystemService.getInstance().removeAllFiles(alreadyExistingModuleFiles);
        } catch (IOException e) {
            LOG.error("Can not remove old module files.", e);
        }
    }

    private List<File> getAllImlFiles(final File dir) {
        final List<File> imlFiles = Arrays.stream(dir.listFiles(
            e -> e.getName().endsWith(HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION)
        )).collect(Collectors.toList());
        return imlFiles;
    }

    private List<File> getModulesChosenForImportFiles(final Iterable<ModuleDescriptor> modulesChosenForImport) {
        final List<File> alreadyExistingModuleFiles = new ArrayList<>();
        for (ModuleDescriptor moduleDescriptor : modulesChosenForImport) {
            final File ideaModuleFile = moduleDescriptor.ideaModuleFile();
            if (ideaModuleFile.exists()) {
                alreadyExistingModuleFiles.add(ideaModuleFile);
            }
        }
        return alreadyExistingModuleFiles;
    }

    @Override
    public String getName() {
        return message("hybris.project.name");
    }

    @Override
    public Icon getIcon() {
        return HybrisIcons.Y.INSTANCE.getLOGO_BLUE();
    }

    @Override
    public void setAllModuleList() {
        moduleList = this.getHybrisProjectDescriptor().getFoundModules();
    }

    @Override
    public List<ModuleDescriptor> getBestMatchingExtensionsToImport(final @Nullable ProjectSettings settings) {
        final List<ModuleDescriptor> allModules = this.getHybrisProjectDescriptor().getFoundModules();
        final List<ModuleDescriptor> moduleToImport = new ArrayList<>();
        final Set<ModuleDescriptor> moduleToCheck = new HashSet<>();
        for (final var moduleDescriptor : allModules) {
            if (moduleDescriptor.isPreselected()) {
                moduleToImport.add(moduleDescriptor);
                moduleDescriptor.setImportStatus(MANDATORY);
                moduleToCheck.add(moduleDescriptor);
            }
        }
        resolveDependency(moduleToImport, moduleToCheck, MANDATORY);

        final Set<String> unusedExtensionNameSet = settings != null
            ? settings.getUnusedExtensions()
            : Collections.emptySet();

        allModules.stream()
            .filter(e -> unusedExtensionNameSet.contains(e.getName()))
            .forEach(e -> {
                moduleToImport.add(e);
                e.setImportStatus(UNUSED);
                moduleToCheck.add(e);
            });
        resolveDependency(moduleToImport, moduleToCheck, UNUSED);

        final Set<String> modulesOnBlackList = settings != null
            ? settings.getModulesOnBlackList()
            : Collections.emptySet();

        return moduleToImport.stream()
            .filter(e -> !modulesOnBlackList.contains(e.getRelativePath()))
            .sorted(Comparator.nullsLast(Comparator.comparing(ModuleDescriptor::getName)))
            .collect(Collectors.toList());
    }

    @Override
    public void setCoreStepModuleList() {
        moduleList = this.getHybrisProjectDescriptor()
            .getFoundModules()
            .stream()
            .filter(Predicate.not(ExternalModuleDescriptor.class::isInstance))
            .collect(Collectors.toList());
    }

    @Override
    public void setExternalStepModuleList() {
        moduleList = this.getHybrisProjectDescriptor()
            .getFoundModules()
            .stream()
            .filter(ExternalModuleDescriptor.class::isInstance)
            .collect(Collectors.toList());
    }

    @Override
    public void setHybrisModulesToImport(final List<ModuleDescriptor> hybrisModules) {
        _hybrisModulesToImport = hybrisModules;
        try {
            setList(hybrisModules);
        } catch (ConfigurationException e) {
            LOG.error(e);
            // no-op already validated
        }
    }

    @Override
    public List<ModuleDescriptor> getHybrisModulesToImport() {
        return _hybrisModulesToImport;
    }

    @Override
    public List<ModuleDescriptor> getList() {
        if (moduleList == null) {
            setAllModuleList();
        }
        return moduleList;
    }

    @Override
    public void setList(final List<ModuleDescriptor> list) throws ConfigurationException {
        final var hybrisProjectDescriptor = getHybrisProjectDescriptor();

        final var chosenForImport = new ArrayList<>(list);
        final var alreadyOpenedModules = isUpdate()
            ? hybrisProjectDescriptor.getAlreadyOpenedModules() // TODO: so what's the purpose if on refresh we remove modules in the ProjectRefreshAction.removeOldProjectData(project);
            : Collections.emptySet();
        chosenForImport.removeAll(alreadyOpenedModules);

        hybrisProjectDescriptor.setChosenModuleDescriptors(chosenForImport);
    }

    @Override
    public boolean isMarked(final ModuleDescriptor element) {
        return false;
    }

    @Override
    public boolean validate(@Nullable final Project currentProject, @NotNull final Project project) {
        return super.validate(currentProject, project);
    }

    private void resolveDependency(
        final List<ModuleDescriptor> moduleToImport,
        final Set<ModuleDescriptor> moduleToCheck,
        final ModuleDescriptorImportStatus selectionMode
    ) {
        while (!moduleToCheck.isEmpty()) {
            final ModuleDescriptor currentModule = moduleToCheck.iterator().next();
            if (currentModule instanceof final YModuleDescriptor yModuleDescriptor) {
                for (final ModuleDescriptor moduleDescriptor : yModuleDescriptor.getAllDependencies()) {
                    if (!moduleToImport.contains(moduleDescriptor)) {
                        moduleToImport.add(moduleDescriptor);
                        moduleDescriptor.setImportStatus(selectionMode);
                        moduleToCheck.add(moduleDescriptor);
                    }
                }
            }
            moduleToCheck.remove(currentModule);
        }
    }

}
