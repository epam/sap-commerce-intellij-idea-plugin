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
import com.intellij.idea.plugin.hybris.impex.psi.references.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.impex.psi.references.result.ItemResolveResult
import com.intellij.idea.plugin.hybris.impex.psi.references.result.RelationResolveResult
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelAccess
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.result.WidgetResolveResult
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class CngWidgetReference(element: PsiElement) : PsiReferenceBase.Poly<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val lookingForName = value

        return CngMetaModelAccess.getInstance(element.project).getMetaModel()
            .widgets[lookingForName]
            ?.retrieveDom()
            ?.let { arrayOf(WidgetResolveResult(it)) }
            ?: emptyArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        if (resolveResults.size != 1) return null

        return with (resolveResults[0]) {
            if (this.isValidResult) return@with this.element
            return@with null
        }
    }

}
