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
package com.intellij.idea.plugin.hybris.flexibleSearch.refactoring.rename.processor

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchPsiNamedElement
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.rename.UnresolvableCollisionUsageInfo
import com.intellij.usageView.UsageInfo

class FlexibleSearchRenameProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement) = element is FlexibleSearchPsiNamedElement

    override fun findReferences(element: PsiElement, searchScope: SearchScope, searchInCommentsAndStrings: Boolean) = findElements(element, element.text)
        .mapNotNull { it.reference }
        .toMutableList()

    override fun renameElement(element: PsiElement, newName: String, usages: Array<out UsageInfo>, listener: RefactoringElementListener?) {
        with(element as FlexibleSearchPsiNamedElement) {
            this.setName(newName)

            usages
                .mapNotNull { it.reference }
                .forEach { it.handleElementRename(newName) }

            listener?.elementRenamed(this)
        }
    }

    override fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val newRenames = allRenames
            .mapNotNull { (element, newName) ->
                findElements(element, element.text)
                    .map { it to newName }
            }
            .flatten()
            .toMap()

        allRenames.clear()
        allRenames.putAll(newRenames)

    }

    override fun findCollisions(element: PsiElement, newName: String, allRenames: MutableMap<out PsiElement, String>, result: MutableList<UsageInfo>) {
        allRenames
            .forEach { (element, newName) ->
                findElements(element, newName)
                    .forEach {
                        result.add(object : UnresolvableCollisionUsageInfo(it, element) {
                            override fun getDescription() = HybrisI18NBundleUtils.message("hybris.fxs.refactoring.rename.existing.tableAlias.conflict", newName)
                        })
                    }
            }
    }

    private fun findElements(element: PsiElement, newText: String): Array<PsiElement> = element.containingFile
        ?.let { file ->
            PsiTreeUtil.collectElements(file) {
                canProcessElement(it) && it.textMatches(newText)
            }
        }
        ?: emptyArray()

}
