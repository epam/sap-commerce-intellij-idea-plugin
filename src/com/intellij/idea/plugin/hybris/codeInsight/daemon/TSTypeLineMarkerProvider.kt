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
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.type.utils.ModelsUtils
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import javax.swing.Icon

class TSTypeLineMarkerProvider : AbstractTSItemLineMarkerProvider<PsiClass>() {

    override fun canProcess(psi: PsiElement) = psi is PsiClass
    override fun getTooltipText() = HybrisI18NBundleUtils.message("hybris.editor.gutter.item.class.tooltip.navigate.declaration")
    override fun getPopupTitle() = HybrisI18NBundleUtils.message("hybris.editor.gutter.bean.type.navigate.choose.class.title")
    override fun getEmptyPopupText() = HybrisI18NBundleUtils.message("hybris.editor.gutter.navigate.no.matching.beans")

    override fun collectDeclarations(
        psi: PsiClass
    ): RelatedItemLineMarkerInfo<PsiElement>? {
        val name = cleanSearchName(psi.name)
        val psiNameIdentifier = psi.nameIdentifier

        if (ModelsUtils.isModelFile(psi)) {
            return collectItemTypes(TSMetaModelAccess.getInstance(psi.project).findMetaItemByName(name), psiNameIdentifier, HybrisIcons.TS_ITEM)
        } else if (ModelsUtils.isEnumFile(psi)) {
            return collectItemTypes(TSMetaModelAccess.getInstance(psi.project).findMetaEnumByName(name), psiNameIdentifier, HybrisIcons.TS_ENUM)
        }
        return null
    }

    private fun collectItemTypes(meta: TSGlobalMetaClassifier<*>?, psiNameIdentifier: PsiIdentifier?, icon: Icon) = meta
        ?.retrieveAllDoms()
        ?.mapNotNull { it.xmlElement }
        ?.toList()
        ?.let { createTargetsWithGutterIcon(psiNameIdentifier, it, icon) }

}
