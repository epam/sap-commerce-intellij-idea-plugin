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
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelStateService
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngModificationTracker
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.result.WidgetDefinitionResolveResult
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class CngWidgetDefinitionReference(element: PsiElement) : PsiReferenceBase.Poly<PsiElement>(element), PsiPolyVariantReference,
    HighlightedReference {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, CngWidgetDefinitionReference>>("HYBRIS_CNGWIDGETDEFINITIONREFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, CngWidgetDefinitionReference> { ref ->
            val element = ref.element
            val lookingForName = ref.value
            val project = element.project

            val result = project.service<CngMetaModelStateService>().get()
                .widgetDefinitions[lookingForName]
                ?.let { PsiUtils.getValidResults(arrayOf(WidgetDefinitionResolveResult(it))) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                project.service<CngModificationTracker>(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}
