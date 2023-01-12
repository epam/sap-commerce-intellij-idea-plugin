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
import com.intellij.codeInspection.ProblemDescriptorBase;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PsiNavigateUtil;
import org.jetbrains.annotations.NotNull;

public class XmlAddAttributeQuickFix implements LocalQuickFix {

    private final String myFixName;
    private final String myAttributeName;

    public XmlAddAttributeQuickFix(
        final String attributeName
    ) {

        myFixName = HybrisI18NBundleUtils.message("hybris.inspections.fix.xml.AddAttribute", attributeName);
        myAttributeName = attributeName;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return myFixName;
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        final PsiElement currentElement = descriptor.getPsiElement();

        if (currentElement instanceof XmlTag) {
            final XmlTag currentTag = (XmlTag) currentElement;
            final XmlAttribute xmlAttribute = currentTag.setAttribute(myAttributeName, "");
            navigateIfNotPreviewMode(descriptor, xmlAttribute.getValueElement());
        }
    }

    private void navigateIfNotPreviewMode(final ProblemDescriptor descriptor, final PsiElement psiElement) {
        if (descriptor instanceof ProblemDescriptorBase) {
            PsiNavigateUtil.navigate(psiElement);
        }
    }
}
