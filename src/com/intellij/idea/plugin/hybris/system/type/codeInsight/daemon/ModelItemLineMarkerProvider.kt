/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.codeInsight.daemon.AbstractHybrisClassLineMarkerProvider
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.util.TSUtils
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import javax.swing.Icon

class ModelItemLineMarkerProvider : AbstractHybrisClassLineMarkerProvider<PsiClass>() {

    override fun getName() = message("hybris.editor.gutter.ts.model.item.name")
    override fun getIcon(): Icon = HybrisIcons.TypeSystem.ITEM
    override fun canProcess(psi: PsiClass) = TSUtils.isItemModelFile(psi)
    override fun tryCast(psi: PsiElement) = (psi as? PsiClass)
        ?.takeIf { it.nameIdentifier != null }

    override fun collectDeclarations(psi: PsiClass) = TSMetaModelAccess.getInstance(psi.project)
        .findMetaItemByName(TSUtils.cleanItemModelSearchName(psi.name))
        ?.retrieveAllDoms()
        ?.mapNotNull { it.code.xmlAttributeValue }
        ?.takeIf { it.isNotEmpty() }
        ?.let {
            NavigationGutterIconBuilder
                .create(icon)
                .setTargets(it)
                .setPopupTitle(message("hybris.editor.gutter.ts.model.item.popup.title"))
                .setTooltipText(message("hybris.editor.gutter.ts.model.item.tooltip.text"))
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(psi.nameIdentifier!!)
        }
        ?.let { listOf(it) }
        ?: emptyList()

}
