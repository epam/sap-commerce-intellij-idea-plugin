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

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.PsiNavigateUtil

class XmlAddAttributeQuickFix(private val attributeName: String) : LocalQuickFix {

    override fun getFamilyName() = message("hybris.inspections.fix.xml.AddAttribute", attributeName)

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val currentElement = descriptor.psiElement

        if (currentElement is XmlTag) {
            val xmlAttribute = currentElement.setAttribute(attributeName, "")
            xmlAttribute.valueElement?.let { navigateIfNotPreviewMode(descriptor, it) }
        }
    }

    private fun navigateIfNotPreviewMode(descriptor: ProblemDescriptor, psiElement: PsiElement) {
        if (descriptor is ProblemDescriptorBase) {
            PsiNavigateUtil.navigate(psiElement)
        }
    }
}
