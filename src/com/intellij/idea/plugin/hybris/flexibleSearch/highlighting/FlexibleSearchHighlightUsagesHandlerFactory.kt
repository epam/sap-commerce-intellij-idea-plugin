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
package com.intellij.idea.plugin.hybris.flexibleSearch.highlighting

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase
import com.intellij.featureStatistics.ProductivityFeatureNames
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchSelectedTableName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableAliasName
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Consumer

class FlexibleSearchHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactoryBase() {

    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile, target: PsiElement) =
        if (target.parent is FlexibleSearchTableAliasName || target.parent is FlexibleSearchSelectedTableName) {
            FxsTableAliasHighlightMacrosHandler(editor, file, target)
        } else {
            null
        }

    class FxsTableAliasHighlightMacrosHandler(editor: Editor, file: PsiFile, val target: PsiElement) : HighlightUsagesHandlerBase<PsiElement>(editor, file) {

        override fun getFeatureId() = ProductivityFeatureNames.CODEASSISTS_HIGHLIGHT_RETURN
        override fun getTargets() = mutableListOf(target)
        override fun selectTargets(targets: MutableList<out PsiElement>, selectionConsumer: Consumer<in MutableList<out PsiElement>>) {
            selectionConsumer.consume(targets)
        }

        override fun computeUsages(targets: MutableList<out PsiElement>) {
            val file = target.containingFile

            PsiTreeUtil.collectElements(file) {
                (it is FlexibleSearchTableAliasName || it is FlexibleSearchSelectedTableName)
                    && it.textMatches(target)
            }
                .forEach { addOccurrence(it) }
        }

    }
}
