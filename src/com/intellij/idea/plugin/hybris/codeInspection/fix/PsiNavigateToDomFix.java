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

package com.intellij.idea.plugin.hybris.codeInspection.fix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.openapi.project.Project;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

public class PsiNavigateToDomFix<D extends DomElement> implements LocalQuickFix {

    private final String myFixName;
    private final DomAnchor<D> myDomAnchor;

    public PsiNavigateToDomFix(final DomAnchor<D> domAnchor) {
        myFixName = HybrisI18NBundleUtils.message("hybris.inspections.fix.psi.NavigateToAnchor");
        myDomAnchor = domAnchor;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return myFixName;
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        PsiNavigateUtil.navigate(myDomAnchor.getPsiElement());
    }
}
