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

package com.intellij.idea.plugin.hybris.tools.remote.console;

import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolePanelView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class HybrisConsoleToolWindowFactory implements ToolWindowFactory {

    public static final String ID = "Hybris Console";

    @Override
    public void createToolWindowContent(
        @NotNull final Project project, @NotNull final ToolWindow toolWindow
    ) {
        HybrisConsolePanelView.Companion.getInstance(project).createToolWindowContent(toolWindow);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull final Project project) {
        return HybrisProjectSettingsComponent.getInstance(project).getState().isHybrisProject();
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }
}
