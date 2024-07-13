/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.flexibleSearch.lang.documentation

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchDefinedTableName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.model.Pointer
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.elementType

class FlexibleSearchDocumentationTarget(val element: PsiElement, private val originalElement: PsiElement?) : DocumentationTarget {

    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createSmartPointer()
        val originalElementPtr = originalElement?.createSmartPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            FlexibleSearchDocumentationTarget(element, originalElementPtr?.dereference())
        }
    }

    override fun computePresentation(): TargetPresentation {
        val virtualFile = element.containingFile.virtualFile
        return TargetPresentation.builder(element.text)
            .locationText(virtualFile.name, virtualFile.fileType.icon)
            .presentation()
    }

    override fun computeDocumentationHint() = computeLocalDocumentation(element)
    override fun computeDocumentation() = computeLocalDocumentation(element)
        ?.let { DocumentationResult.documentation(it) }

    override val navigatable: Navigatable?
        get() = element as? Navigatable

    private fun computeLocalDocumentation(element: PsiElement): String? {
        if (element.elementType == FlexibleSearchTypes.IDENTIFIER && element.parent is FlexibleSearchDefinedTableName) {
            return try {
                TSMetaModelAccess.getInstance(element.project).findMetaClassifierByName(element.text)
                    ?.documentation()
            } catch (_: ProcessCanceledException) {
                message("hybris.documentation.not.available.during.indexing")
            }
        }

        return null
    }
}