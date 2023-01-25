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

package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.childrenOfType
import java.util.*

class TSAttributeFieldLineMarkerProvider : AbstractTSAttributeLineMarkerProvider<PsiField>() {

    override fun canProcess(psi: PsiElement) = psi is PsiField
            && psi.type is PsiClassReferenceType
            && (psi.type as PsiClassReferenceType).name == String::class.java.simpleName

    override fun collect(meta: TSGlobalMetaItem, psi: PsiField) = psi.childrenOfType<PsiLiteralExpression>()
        .map {
            val name = it.value.toString()
            val nameIdentifier = psi.nameIdentifier
            getPsiElementRelatedItemLineMarkerInfo(meta, name, nameIdentifier)
                .or { getPsiElementRelatedRelationLineMarkerInfo(name, nameIdentifier) }
        }
        .firstOrNull()
        ?: Optional.empty()

    private fun getPsiElementRelatedRelationLineMarkerInfo(
        name: String,
        nameIdentifier: PsiIdentifier
    ): Optional<RelatedItemLineMarkerInfo<PsiElement>> {
        val dom = TSMetaModelAccess.getInstance(nameIdentifier.project).findMetaRelationByName(name)
            ?.retrieveDom()
            ?.xmlElement
            ?: return Optional.empty<RelatedItemLineMarkerInfo<PsiElement>>()

        return Optional.of(createTargetsWithGutterIcon(nameIdentifier, listOf(dom), HybrisIcons.RELATION))
    }
}