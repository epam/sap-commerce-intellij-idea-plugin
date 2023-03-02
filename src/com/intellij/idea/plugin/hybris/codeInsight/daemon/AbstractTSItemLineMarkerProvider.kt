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
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlElement
import javax.swing.Icon

@Suppress("UNCHECKED_CAST")
abstract class AbstractTSItemLineMarkerProvider<T : PsiElement> : RelatedItemLineMarkerProvider() {

    protected abstract fun canProcess(psi: PsiElement): Boolean
    protected abstract fun collectDeclarations(psi: T): RelatedItemLineMarkerInfo<PsiElement>?
    protected abstract fun getEmptyPopupText(): String
    protected abstract fun getPopupTitle(): String
    protected abstract fun getTooltipText(): String

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>?>
    ) {
        if (!canProcess(element)) return
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return
        if (HybrisModuleDescriptor.getDescriptorType(module) != HybrisModuleDescriptorType.PLATFORM) return

        CachedValuesManager.getCachedValue(element) {
            CachedValueProvider.Result.create(
                collectDeclarations(element as T),
                TSMetaModelAccess.getInstance(element.project).getMetaModel()
            )
        }
            ?.let { result.add(it) }
    }

    protected fun createTargetsWithGutterIcon(
        psiIdentifier: PsiIdentifier?,
        list: Collection<XmlElement?>?,
        icon: Icon?
    ) = NavigationGutterIconBuilder
        .create(icon!!)
        .setTargets(list!!)
        .setEmptyPopupText(getEmptyPopupText())
        .setPopupTitle(getPopupTitle())
        .setTooltipText(getTooltipText())
        .setAlignment(GutterIconRenderer.Alignment.LEFT)
        .createLineMarkerInfo(psiIdentifier!!)

    protected fun cleanSearchName(searchName: String?): String? {
        if (searchName == null) return null

        val idx = searchName.lastIndexOf(HybrisConstants.MODEL_SUFFIX)
        return if (idx == -1) {
            searchName
        } else searchName.substring(0, idx)
    }
}
