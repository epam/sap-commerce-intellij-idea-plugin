/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.lang.refactoring

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.rename.UnresolvableCollisionUsageInfo
import com.intellij.usageView.UsageInfo

class ImpExRenamePsiElementProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement) = element is ImpexMacroNameDec
        || element is ImpexDocumentIdDec
        || element is ImpexDocumentIdUsage
        || (element is ImpexMacroUsageDec && !element.text.startsWith(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX))

    override fun findReferences(element: PsiElement, searchScope: SearchScope, searchInCommentsAndStrings: Boolean): Collection<PsiReference> {
        return findElements(element, element.text)
            .mapNotNull { it.reference }
            .toMutableList()
    }

    override fun renameElement(element: PsiElement, newName: String, usages: Array<out UsageInfo>, listener: RefactoringElementListener?) {
        with(element as ImpexPsiNamedElement) {
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
                            override fun getDescription() = when (element.elementType) {
                                ImpexTypes.MACRO_NAME_DECLARATION,
                                ImpexTypes.MACRO_USAGE -> HybrisI18NBundleUtils.message("hybris.impex.refactoring.rename.existing.macroName.conflict", newName)

                                else -> HybrisI18NBundleUtils.message("hybris.impex.refactoring.rename.existing.conflict", newName)
                            }
                        })
                    }
            }
    }

    private fun findElements(renameElement: PsiElement, newText: String): Array<PsiElement> = renameElement.containingFile
        ?.let { file ->
            val filter = when (renameElement) {
                is ImpexMacroNameDec,
                is ImpexMacroUsageDec -> PsiElementFilter { element ->
                    if (element is ImpexMacroNameDec || element is ImpexMacroUsageDec) {
                        element.text == newText
                    } else {
                        false
                    }
                }

                is ImpexDocumentIdDec,
                is ImpexDocumentIdUsage -> PsiElementFilter { element ->
                    if (element is ImpexDocumentIdDec || element is ImpexDocumentIdUsage) {
                        element.text == newText
                    } else {
                        false
                    }
                }

                else -> return@let null
            }

            PsiTreeUtil.collectElements(file, filter)
        }
        ?: emptyArray()
}