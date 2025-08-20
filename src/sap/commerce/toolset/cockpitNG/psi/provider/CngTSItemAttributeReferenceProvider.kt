/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.cockpitNG.psi.provider

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.cockpitNG.model.listView.ListColumn
import sap.commerce.toolset.cockpitNG.psi.reference.CngTSItemAttributeReference

class CngTSItemAttributeReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<PsiReference> = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        val references = element.parentOfType<XmlTag>()
            ?.let { DomManager.getDomManager(element.project).getDomElement(it) }
            ?.asSafely<ListColumn>()
            ?.springBean
            ?.takeUnless { it.stringValue == null }
            ?.let { emptyArray() }
            ?: createReferences(element)

        CachedValueProvider.Result.createSingleDependency(
            references,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }

    private fun createReferences(element: PsiElement): Array<PsiReference> {
        var from = 1
        var previousReference: CngTSItemAttributeReference? = null

        val value = StringUtil.unquoteString(element.text)
        return value
            .takeIf { it.isNotBlank() }
            ?.split('.')
            ?.map {
                val textRange = TextRange.from(from + it.length - it.trimStart().length, it.trim().length)
                from += it.length + 1
                textRange
            }
            ?.map {
                val nextReference = CngTSItemAttributeReference(element, it, previousReference)
                previousReference = nextReference
                nextReference
            }
            ?.takeIf { it.isNotEmpty() }
            ?.toTypedArray()
            ?: arrayOf(CngTSItemAttributeReference(element, TextRange.from(from, value.length)))
    }

}