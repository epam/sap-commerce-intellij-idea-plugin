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

import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import sap.commerce.toolset.properties.meta.CxPropertyCollector

class CxPropertyDocumentationTarget(private val element: PsiElement, private val key: String) : DocumentationTarget {

    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createSmartPointer()

        return Pointer {
            elementPtr.dereference()?.let { CxPropertyDocumentationTarget(it, key) }
        }
    }

    override fun computePresentation(): TargetPresentation {
        val virtualFile = element.containingFile?.virtualFile

        return TargetPresentation.builder(key)
            .let { if (virtualFile == null) it else it.locationText(virtualFile.name, virtualFile.fileType.icon) }
            .presentation()
    }

    override fun computeDocumentationHint() = documentation()
    override fun computeDocumentation() = documentation()?.let { DocumentationResult.documentation(it) }

    override val navigatable: Navigatable?
        get() = element as? Navigatable

    private fun documentation() = CxPropertyDocumentation.of(key, CxPropertyCollector.getInstance(element.project).collect())
}
