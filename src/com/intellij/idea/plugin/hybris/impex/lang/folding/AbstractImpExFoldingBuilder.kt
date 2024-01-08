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

package com.intellij.idea.plugin.hybris.impex.lang.folding

import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement

abstract class AbstractImpExFoldingBuilder : FoldingBuilderEx() {

    abstract fun buildFoldRegionsInternal(psi: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor>

    override fun buildFoldRegions(psi: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val foldingEnabled = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(psi.project).getState()
            .impexSettings
            .folding
            .enabled
        return if (!foldingEnabled) emptyArray()
        else buildFoldRegionsInternal(psi, document, quick)
    }
}