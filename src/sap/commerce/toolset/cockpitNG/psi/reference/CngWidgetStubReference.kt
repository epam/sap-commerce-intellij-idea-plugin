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

package sap.commerce.toolset.cockpitNG.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.cockpitNG.meta.CngMetaModelStateService
import sap.commerce.toolset.cockpitNG.meta.CngModificationTracker
import sap.commerce.toolset.cockpitNG.psi.reference.result.ActionDefinitionResolveResult
import sap.commerce.toolset.cockpitNG.psi.reference.result.EditorDefinitionResolveResult
import sap.commerce.toolset.psi.getValidResults

class CngWidgetStubReference(element: PsiElement) : PsiReferenceBase.Poly<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun calculateDefaultRangeInElement() = TextRange.from(STUB_LENGTH + 1, element.textLength - STUB_LENGTH - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { getValidResults(it) }

    companion object {
        private const val STUB_LENGTH = HybrisConstants.COCKPIT_NG_WIDGET_ID_STUB.length

        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, CngWidgetStubReference>>("HYBRIS_CNGWIDGETSTUBREFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, CngWidgetStubReference> { ref ->
            val element = ref.element
            val value = ref.value
            val project = element.project
            val metaModel = CngMetaModelStateService.state(project)

            val result = metaModel
                .editorDefinitions[value]
                ?.let { getValidResults(arrayOf(EditorDefinitionResolveResult(it))) }
                ?: metaModel.actionDefinitions[value]
                    ?.let { getValidResults(arrayOf(ActionDefinitionResolveResult(it))) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                CngModificationTracker.getInstance(project), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}
