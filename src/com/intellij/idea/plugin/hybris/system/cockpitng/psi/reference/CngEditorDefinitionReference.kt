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
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelAccess
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.result.EditorDefinitionResolveResult
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase

class CngEditorDefinitionReference(element: PsiElement, textRange: TextRange) : PsiReferenceBase.Poly<PsiElement>(element, textRange, false),
    PsiPolyVariantReference, HighlightedReference {

    override fun multiResolve(incompleteCode: Boolean) = CngMetaModelAccess.getInstance(element.project).getMetaModel()
        .editorDefinitions[value]
        ?.let { PsiUtils.getValidResults(arrayOf(EditorDefinitionResolveResult(it))) }
        ?: emptyArray()

}
