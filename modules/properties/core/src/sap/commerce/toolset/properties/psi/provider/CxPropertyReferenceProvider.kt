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

package sap.commerce.toolset.properties.psi.provider

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyPlaceholder
import sap.commerce.toolset.properties.psi.reference.CxPropertyReference

/** Turns every `${...}` written in a property value into a reference to the property it names. */
class CxPropertyReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<out PsiReference> {
        if (element.project.isNotHybrisProject) return PsiReference.EMPTY_ARRAY
        if (!partOfTheChain(element)) return PsiReference.EMPTY_ARRAY

        return referencesOf(element)
    }

    private fun partOfTheChain(element: PsiElement): Boolean {
        val file = element.containingFile?.originalFile?.virtualFile ?: return false

        return CxPropertyCollector.getInstance(element.project)
            .collect()
            .sources
            .any { it.file == file }
    }

    private fun referencesOf(element: PsiElement) = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        val references = CxPropertyPlaceholder.of(element.text)
            .map { CxPropertyReference(element, TextRange(it.keyRange.first, it.keyRange.last + 1), it.key) }
            .toTypedArray<PsiReference>()

        CachedValueProvider.Result.createSingleDependency(references, element)
    }
}
