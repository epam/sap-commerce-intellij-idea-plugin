/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.type.codeInsight.daemon

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

class ModelItemAttributeMethodLineMarkerProvider : AbstractModelAttributeLineMarkerProvider<PsiMethod>() {

    override fun getName() = message("hybris.editor.gutter.ts.model.item.attribute.method.name")
    override fun tryCast(psi: PsiElement) = psi as? PsiMethod

    override fun collect(meta: TSGlobalMetaItem, psi: PsiMethod) = psi.getAnnotation(HybrisConstants.CLASS_FQN_ANNOTATION_ACCESSOR)
        ?.parameterList
        ?.attributes
        ?.filter { it.literalValue != null && it.nameIdentifier != null }
        ?.firstOrNull { it.name == Attribute.QUALIFIER }
        ?.let { getPsiElementItemLineMarkerInfo(meta, it.literalValue!!, it.nameIdentifier!!) }
        ?.let { listOf(it) }
        ?: emptyList()
}