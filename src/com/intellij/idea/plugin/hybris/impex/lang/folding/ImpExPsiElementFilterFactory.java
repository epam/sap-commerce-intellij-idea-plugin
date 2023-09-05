/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.lang.folding;

import com.intellij.idea.plugin.hybris.impex.lang.folding.util.ImpExSimpleFoldingBlocksFilter;
import com.intellij.idea.plugin.hybris.impex.lang.folding.util.ImpExSmartFoldingBlocksFilter;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiElementFilter;
import org.jetbrains.annotations.NotNull;

public final class ImpExPsiElementFilterFactory {

    private ImpExPsiElementFilterFactory() throws IllegalAccessException {
        throw new IllegalAccessException("Should never be accessed.");
    }

    public static PsiElementFilter getPsiElementFilter(final @NotNull Project project) {
        return isUseSmartFolding(project)
            ? ApplicationManager.getApplication().getService(ImpExSmartFoldingBlocksFilter.class)
            : ApplicationManager.getApplication().getService(ImpExSimpleFoldingBlocksFilter.class);
    }

    private static boolean isUseSmartFolding(final @NotNull Project project) {
        return HybrisProjectSettingsComponent.getInstance(project).getState()
            .getImpexSettings()
            .getFolding()
            .getUseSmartFolding();
    }

}
