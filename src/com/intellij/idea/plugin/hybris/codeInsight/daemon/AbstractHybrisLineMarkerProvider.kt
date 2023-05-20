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

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

abstract class AbstractHybrisLineMarkerProvider<T : PsiElement> : LineMarkerProviderDescriptor() {

    final override fun getLineMarkerInfo(element: PsiElement) = null

    protected abstract fun canProcess(psi: PsiFile): Boolean
    protected abstract fun tryCast(psi: PsiElement): T?
    protected abstract fun collectDeclarations(psi: T): Collection<LineMarkerInfo<PsiElement>>

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        if (!canProcess(elements)) return

        elements
            .mapNotNull { tryCast(it) }
            .flatMap { collectDeclarations(it) }
            .let { result.addAll(it) }
    }

    protected open fun canProcess(elements: MutableList<out PsiElement>): Boolean {
        val psiFile = elements.firstOrNull()?.containingFile
            ?: return false
        if (!HybrisProjectSettingsComponent.getInstance(psiFile.project).isHybrisProject()) return false
        return canProcess(psiFile)
    }
}