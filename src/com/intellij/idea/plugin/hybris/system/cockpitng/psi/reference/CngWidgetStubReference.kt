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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelStateService
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngModificationTracker
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.result.ActionDefinitionResolveResult
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.result.EditorDefinitionResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class CngWidgetStubReference(element: PsiElement) : PsiReferenceBase.Poly<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun calculateDefaultRangeInElement() = TextRange.from(STUB_LENGTH + 1, element.textLength - STUB_LENGTH - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        private const val STUB_LENGTH = HybrisConstants.COCKPIT_NG_WIDGET_ID_STUB.length

        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, CngWidgetStubReference>>("HYBRIS_CNGWIDGETSTUBREFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, CngWidgetStubReference> { ref ->
            val element = ref.element
            val value = ref.value
            val project = element.project
            val metaModel = project.service<CngMetaModelStateService>().get()

            val result = metaModel
                .editorDefinitions[value]
                ?.let { PsiUtils.getValidResults(arrayOf(EditorDefinitionResolveResult(it))) }
                ?: metaModel.actionDefinitions[value]
                    ?.let { PsiUtils.getValidResults(arrayOf(ActionDefinitionResolveResult(it))) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                project.service<CngModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}
