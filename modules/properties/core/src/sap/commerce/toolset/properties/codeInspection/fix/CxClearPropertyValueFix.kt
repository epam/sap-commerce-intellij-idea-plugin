/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.codeInspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import sap.commerce.toolset.properties.meta.CxPropertyCollector

/**
 * Empties a value while keeping the key, for a secret which should never have been written down but whose key is
 * still worth documenting as something the environment has to supply.
 */
class CxClearPropertyValueFix : LocalQuickFix {

    override fun getFamilyName() = "Clear the value"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val property = PsiTreeUtil.getParentOfType(descriptor.psiElement, Property::class.java, false)
            ?: descriptor.psiElement.asSafely<Property>()
            ?: return

        property.setValue("")

        CxPropertyCollector.getInstance(project).resetCache()
    }
}
