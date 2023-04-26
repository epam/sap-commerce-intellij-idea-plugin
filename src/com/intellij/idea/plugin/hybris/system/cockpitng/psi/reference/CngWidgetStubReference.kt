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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelAccess
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.result.ActionDefinitionResolveResult
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.result.EditorDefinitionResolveResult
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class CngWidgetStubReference(element: PsiElement) : PsiReferenceBase.Poly<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun calculateDefaultRangeInElement() = TextRange.from(stubLength + 1, element.textLength - stubLength - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val metaModel = CngMetaModelAccess.getInstance(element.project).getMetaModel()
        return metaModel
            .editorDefinitions[value]
            ?.let { PsiUtils.getValidResults(arrayOf(EditorDefinitionResolveResult(it))) }
            ?: metaModel.actionDefinitions[value]
                ?.let { PsiUtils.getValidResults(arrayOf(ActionDefinitionResolveResult(it))) }
            ?: emptyArray()
    }

    companion object {
        private const val stubLength = HybrisConstants.COCKPIT_NG_WIDGET_ID_STUB.length
    }
}
