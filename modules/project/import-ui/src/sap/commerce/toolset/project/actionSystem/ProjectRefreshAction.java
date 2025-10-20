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

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisIcons;
import sap.commerce.toolset.project.ProjectRefreshService;
import sap.commerce.toolset.settings.WorkspaceSettings;

import static sap.commerce.toolset.HybrisI18NBundleUtils.message;

public class ProjectRefreshAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent e) {
        final var project = e.getProject();

        if (project == null) return;

        try {
            ProjectRefreshService.Companion.getInstance(project).refresh();
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
}
