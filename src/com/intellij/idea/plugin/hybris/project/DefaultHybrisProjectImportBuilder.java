/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.project;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.VirtualFileSystemService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.notifications.Notifications;
import com.intellij.idea.plugin.hybris.project.configurators.ConfiguratorFactory;
import com.intellij.idea.plugin.hybris.project.configurators.PostImportConfigurator;
import com.intellij.idea.plugin.hybris.project.descriptors.*;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.RootModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.tasks.ImportProjectProgressModalWindow;
import com.intellij.idea.plugin.hybris.project.tasks.SearchModulesRootsTaskModalWindow;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.idea.plugin.hybris.startup.HybrisProjectImportStartupActivity;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import kotlin.Triple;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.GuardedBy;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;
import static com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorImportStatus.MANDATORY;
import static com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorImportStatus.UNUSED;

public class DefaultHybrisProjectImportBuilder extends AbstractHybrisProjectImportBuilder {

    private static final Logger LOG = Logger.getInstance(DefaultHybrisProjectImportBuilder.class);

    protected final Lock lock = new ReentrantLock();

    @Nullable
    @GuardedBy("lock")
    protected volatile HybrisProjectDescriptor hybrisProjectDescriptor;
    protected volatile boolean refresh;
    private List<ModuleDescriptor> moduleList;
    private List<ModuleDescriptor> hybrisModulesToImport;

    @Override
    @Nullable
    public Project createProject(final String name, final String path) {
        final Project project = super.createProject(name, path);
        getHybrisProjectDescriptor().setHybrisProject(project);
        return project;
    }

    @Override
    public void setRootProjectDirectory(@NotNull final File directory) {
        Validate.notNull(directory);

        LOG.info("setting RootProjectDirectory to "+directory.getAbsolutePath());
        ProgressManager.getInstance().run(new SearchModulesRootsTaskModalWindow(
            directory, this.getHybrisProjectDescriptor()
        ));

        this.setFileToImport(directory.getAbsolutePath());
    }

    @Override
    public void cleanup() {
        super.cleanup();

        this.lock.lock();

        try {
            this.hybrisProjectDescriptor = null;
        } finally {
            this.lock.unlock();
        }
        refresh = false;
    }

    @Override
    public void setRefresh(final boolean refresh) {
        this.refresh = refresh;
    }

    @NotNull
    @Override
    public HybrisProjectDescriptor getHybrisProjectDescriptor() {
        this.lock.lock();

        try {
            if (null == this.hybrisProjectDescriptor) {
                this.hybrisProjectDescriptor = new DefaultHybrisProjectDescriptor();
                this.hybrisProjectDescriptor.setProject(getCurrentProject());
            }

            //noinspection ConstantConditions
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
        final ModifiableModuleModel model,
        final ModulesProvider modulesProvider,
        final ModifiableArtifactModel artifactModel
    ) {

        final List<Module> modules = new ArrayList<>();

        final HybrisProjectDescriptor hybrisProjectDescriptor = getHybrisProjectDescriptor();
        final List<ModuleDescriptor> allModules = hybrisProjectDescriptor.getModulesChosenForImport();
        if (allModules.isEmpty()) {
            return Collections.emptyList();
        }
        final ConfiguratorFactory configuratorFactory = ApplicationManager.getApplication().getService(ConfiguratorFactory.class);

        this.performProjectsCleanup(allModules);

        new ImportProjectProgressModalWindow(
            project, model, configuratorFactory, hybrisProjectDescriptor, modules
        ).queue();

        if (refresh) {
            PostImportConfigurator.Companion.getInstance(project).configure(hybrisProjectDescriptor, allModules, refresh);
        } else {
            project.putUserData(HybrisProjectImportStartupActivity.Companion.getFinalizeProjectImportKey(), new Triple<>(hybrisProjectDescriptor, allModules, refresh));
        }
        notifyImportNotFinishedYet(project);
        return modules;
    }

    private void notifyImportNotFinishedYet(@NotNull final Project project) {

        final String notificationTitle = refresh
            ? message("hybris.notification.project.refresh.title")
            : message("hybris.notification.project.import.title");

        Notifications.create(NotificationType.INFORMATION, notificationTitle,
                             message("hybris.notification.import.or.refresh.process.not.finished.yet.content"))
                     .notify(project);
    }

    protected void performProjectsCleanup(@NotNull final Iterable<ModuleDescriptor> modulesChosenForImport) {
        Validate.notNull(modulesChosenForImport);
        final List<File> alreadyExistingModuleFiles;
        final File dir = hybrisProjectDescriptor.getModulesFilesDirectory();
        if (dir != null && dir.isDirectory()) {
            alreadyExistingModuleFiles = getAllImlFiles(dir);
        } else {
            alreadyExistingModuleFiles = getModulesChosenForImportFiles(modulesChosenForImport);
        }
        Collections.sort(alreadyExistingModuleFiles);

        try {
            ApplicationManager.getApplication().getService(VirtualFileSystemService.class).removeAllFiles(alreadyExistingModuleFiles);
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
        List<File> alreadyExistingModuleFiles = new ArrayList<>();
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
        return HybrisIcons.Y_LOGO_BLUE;
    }

    @Override
    public void setAllModuleList() {
        moduleList = this.getHybrisProjectDescriptor().getFoundModules();
    }

    @Override
    public List<ModuleDescriptor> getBestMatchingExtensionsToImport(final @Nullable HybrisProjectSettings settings) {
        final List<ModuleDescriptor> allModules = this.getHybrisProjectDescriptor().getFoundModules();
        final List<ModuleDescriptor> moduleToImport = new ArrayList<>();
        final Set<ModuleDescriptor> moduleToCheck = new HashSet<>();
        for (ModuleDescriptor moduleDescriptor : allModules) {
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
                         .filter(Predicate.not(RootModuleDescriptor.class::isInstance))
                         .collect(Collectors.toList());
    }

    @Override
    public void setExternalStepModuleList() {
        moduleList = this.getHybrisProjectDescriptor()
                         .getFoundModules()
                         .stream()
                         .filter(RootModuleDescriptor.class::isInstance)
                         .collect(Collectors.toList());
    }

    @Override
    public void setHybrisModulesToImport(final List<ModuleDescriptor> hybrisModules) {
        hybrisModulesToImport = hybrisModules;
        try {
            setList(hybrisModules);
        } catch (ConfigurationException e) {
            LOG.error(e);
            // no-op already validated
        }
    }

    @Override
    public List<ModuleDescriptor> getHybrisModulesToImport() {
        return hybrisModulesToImport;
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

        final List<ModuleDescriptor> chosenForImport = new ArrayList<ModuleDescriptor>(list);

        chosenForImport.removeAll(this.getHybrisProjectDescriptor().getAlreadyOpenedModules());

        this.getHybrisProjectDescriptor().setModulesChosenForImport(chosenForImport);
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
