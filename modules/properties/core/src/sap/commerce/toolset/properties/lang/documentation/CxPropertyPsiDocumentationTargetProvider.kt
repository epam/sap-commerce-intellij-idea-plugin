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

package sap.commerce.toolset.properties.lang.documentation

import com.intellij.lang.properties.psi.Property
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.properties.meta.CxPropertyCollector

/**
 * Documents a property key wherever one is written: the declaration itself, and the `${...}` references which resolve
 * to it.
 */
class CxPropertyPsiDocumentationTargetProvider : PsiDocumentationTargetProvider {

    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): CxPropertyDocumentationTarget? {
        if (element.project.isNotHybrisProject) return null

        val property = element.asSafely<Property>()
            ?: PsiTreeUtil.getParentOfType(element, Property::class.java, false)
            ?: return null
        val key = property.key ?: return null
        val file = element.containingFile?.originalFile?.virtualFile ?: return null

        if (CxPropertyCollector.getInstance(element.project).collect().sources.none { it.file == file }) return null

        return CxPropertyDocumentationTarget(element, key)
    }
}
