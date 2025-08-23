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

package sap.commerce.toolset.project.actionSystem;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.newProjectWizard.AddModuleWizard;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.Messages;
import com.intellij.projectImport.ProjectImportProvider;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisIcons;
import sap.commerce.toolset.project.HybrisProjectImportProvider;
import sap.commerce.toolset.project.configurator.ProjectRefreshConfigurator;
import sap.commerce.toolset.project.facet.YFacet;
import sap.commerce.toolset.project.settings.ProjectSettings;
import sap.commerce.toolset.project.wizard.RefreshSupport;
import sap.commerce.toolset.settings.WorkspaceSettings;

import static sap.commerce.toolset.HybrisI18NBundleUtils.message;

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
            final AnActionEvent actionEvent = AnActionEvent.createEvent(
                action,
                dataContext,
                null,
                ActionPlaces.UNKNOWN,
                ActionUiKind.NONE,
                null
            );

            ActionUtil.performActionDumbAwareWithCallbacks(action, actionEvent);
        }, ModalityState.nonModal());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent e) {
        final var project = getEventProject(e);

        if (project == null) return;

        removeOldProjectData(project);

        try {
            createWizard(project)
                .getProjectBuilder()
                .commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
        } catch (final ConfigurationException ex) {
            Messages.showErrorDialog(
                project,
                ex.getMessageHtml().toString(),
                message("hybris.project.import.error.unable.to.proceed")
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
        presentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true);
        presentation.setIcon(HybrisIcons.Y.INSTANCE.getLOGO_BLUE());
        presentation.setVisible(WorkspaceSettings.getInstance(project).getHybrisProject());
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    private static void removeOldProjectData(@NotNull final Project project) {
        final var moduleModel = ModuleManager.getInstance(project).getModifiableModel();
        final var projectSettings = ProjectSettings.getInstance(project);
        final var removeExternalModulesOnRefresh = projectSettings.getRemoveExternalModulesOnRefresh();

        for (Module module : moduleModel.getModules()) {
            if (removeExternalModulesOnRefresh || YFacet.Companion.getState(module) != null) {
                moduleModel.disposeModule(module);
            }
        }
        final LibraryTable.ModifiableModel libraryModel = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();

        for (Library library : libraryModel.getLibraries()) {
            libraryModel.removeLibrary(library);
        }
        ApplicationManager.getApplication().runWriteAction(() -> {
            moduleModel.commit();
            libraryModel.commit();
        });

        ProjectRefreshConfigurator.Companion.getEP().getExtensionList()
            .forEach(configurator -> configurator.beforeRefresh(project));
    }

    private AddModuleWizard createWizard(final Project project) throws ConfigurationException {
        final var provider = getHybrisProjectImportProvider();
        final var projectName = project.getName();
        final var jdk = ProjectRootManager.getInstance(project).getProjectSdk();
        final var compilerOutputUrl = CompilerProjectExtension.getInstance(project).getCompilerOutputUrl();
        final var projectSettings = ProjectSettings.getInstance(project);

        final var wizard = new AddModuleWizard(project, project.getBasePath(), provider) {

            @Override
            protected void init() {
                // non GUI mode
            }
        };
        final var wizardContext = wizard.getWizardContext();
        wizardContext.setProjectJdk(jdk);
        wizardContext.setProjectName(projectName);
        wizardContext.setCompilerOutputDirectory(compilerOutputUrl);

        for (final var step : wizard.getSequence().getAllSteps()) {
            if (step instanceof final RefreshSupport refreshStep) {
                refreshStep.refresh(projectSettings);
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
