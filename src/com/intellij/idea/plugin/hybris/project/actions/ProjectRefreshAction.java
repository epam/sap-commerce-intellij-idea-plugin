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

package com.intellij.idea.plugin.hybris.project.actions;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.newProjectWizard.AddModuleWizard;
import com.intellij.ide.util.newProjectWizard.StepSequence;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.gradle.GradleSupport;
import com.intellij.idea.plugin.hybris.project.AbstractHybrisProjectImportBuilder;
import com.intellij.idea.plugin.hybris.project.HybrisProjectImportProvider;
import com.intellij.idea.plugin.hybris.project.wizard.RefreshSupport;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.lang.ant.config.AntBuildFile;
import com.intellij.lang.ant.config.AntConfigurationBase;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.Messages;
import com.intellij.projectImport.ProjectImportProvider;
import org.jetbrains.annotations.NotNull;


/**
 * Created by Martin Zdarsky-Jones on 8/2/17.
 */
public class ProjectRefreshAction extends AnAction {

    public static void triggerAction() {
        final DataManager dataManager = DataManager.getInstance();
        if (dataManager != null) {
            dataManager.getDataContextFromFocusAsync()
                       .onSuccess(ProjectRefreshAction::triggerAction);
        }
    }

    public static void triggerAction(final DataContext dataContext) {
        ApplicationManager.getApplication().invokeLater(() -> {
            final AnAction action = new ProjectRefreshAction();
            final AnActionEvent actionEvent = AnActionEvent.createFromAnAction(
                action,
                null,
                ActionPlaces.UNKNOWN,
                dataContext
            );
            action.actionPerformed(actionEvent);
        }, ModalityState.NON_MODAL);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent anActionEvent) {
        final Project project = getEventProject(anActionEvent);

        if (project == null) {
            return;
        }
        removeOldProjectData(project);

        try {
            final AddModuleWizard wizard = getWizard(project);
            final ProjectBuilder projectBuilder = wizard.getProjectBuilder();

            if (projectBuilder instanceof AbstractHybrisProjectImportBuilder) {
                ((AbstractHybrisProjectImportBuilder) projectBuilder).setRefresh(true);
            }
            projectBuilder.commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
        } catch (final ConfigurationException e) {
            Messages.showErrorDialog(
                project,
                e.getMessage(),
                HybrisI18NBundleUtils.message("hybris.project.import.error.unable.to.proceed")
            );
        }
    }

    @Override
    public void update(final AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Presentation presentation = e.getPresentation();
        if (project == null) {
            presentation.setVisible(false);
            return;
        }
        presentation.setIcon(HybrisIcons.HYBRIS);
        presentation.setVisible(CommonIdeaService.Companion.getInstance().isHybrisProject(project));
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    @Override
    public boolean displayTextInToolbar() {
        return true;
    }

    private static void removeOldProjectData(@NotNull final Project project) {
        final ModifiableModuleModel moduleModel = ModuleManager.getInstance(project).getModifiableModel();

        for (Module module : moduleModel.getModules()) {
            moduleModel.disposeModule(module);
        }
        final LibraryTable.ModifiableModel libraryModel = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();

        for (Library library : libraryModel.getLibraries()) {
            libraryModel.removeLibrary(library);
        }
        ApplicationManager.getApplication().runWriteAction(() -> {
            moduleModel.commit();
            libraryModel.commit();
        });
        final GradleSupport gradleSupport = GradleSupport.getInstance();

        if (gradleSupport != null) {
            gradleSupport.clearLinkedProjectSettings(project);
        }
        final AntConfigurationBase antConfiguration = AntConfigurationBase.getInstance(project);

        for (AntBuildFile antBuildFile : antConfiguration.getBuildFiles()) {
            antConfiguration.removeBuildFile(antBuildFile);
        }
    }

    private AddModuleWizard getWizard(final Project project) throws ConfigurationException {
        final ProjectImportProvider provider = getHybrisProjectImportProvider();
        final String projectName = project.getName();
        final Sdk jdk = ProjectRootManager.getInstance(project).getProjectSdk();
        final String compilerOutputUrl = CompilerProjectExtension.getInstance(project).getCompilerOutputUrl();
        final HybrisProjectSettings settings = HybrisProjectSettingsComponent.getInstance(project).getState();

        final AddModuleWizard wizard = new AddModuleWizard(null, project.getBasePath(), provider) {

            @Override
            protected void init() {
                // non GUI mode
            }
        };
        final WizardContext wizardContext = wizard.getWizardContext();
        wizardContext.setProjectJdk(jdk);
        wizardContext.setProjectName(projectName);
        wizardContext.setCompilerOutputDirectory(compilerOutputUrl);
        final StepSequence stepSequence = wizard.getSequence();
        for (ModuleWizardStep step : stepSequence.getAllSteps()) {
            if (step instanceof RefreshSupport) {
                ((RefreshSupport) step).refresh(settings);
            }
        }
        return wizard;
    }

    private ProjectImportProvider getHybrisProjectImportProvider() {
        for (final ProjectImportProvider provider : ProjectImportProvider.PROJECT_IMPORT_PROVIDER.getExtensionsIfPointIsRegistered()) {
            if (provider instanceof HybrisProjectImportProvider) {
                return provider;
            }
        }
        return null;
    }
}
