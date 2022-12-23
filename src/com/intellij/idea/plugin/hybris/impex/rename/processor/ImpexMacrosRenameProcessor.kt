/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.impex.rename.processor

import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroNameDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexPsiNamedElement
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexMacrosReferenceBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class ImpexMacrosRenameProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement): Boolean {
        return element is ImpexMacroNameDec || element is ImpexMacroUsageDec
    }

    override fun findReferences(element: PsiElement, searchScope: SearchScope, searchInCommentsAndStrings: Boolean): MutableCollection<PsiReference> {
        val file = element.containingFile
        val psiElements = PsiTreeUtil.collectElements(file) { el -> (el is ImpexMacroNameDec || el is ImpexMacroUsageDec) && el.text == element.text }

        return psiElements.map { ImpexMacrosReferenceBase(it) }.toMutableList()
    }

    override fun renameElement(element: PsiElement, newName: String, usages: Array<out UsageInfo>, listener: RefactoringElementListener?) {
        val macrosName = element as ImpexPsiNamedElement

        macrosName.setName(newName)

        usages.forEach { it.reference?.handleElementRename(newName) }

        listener?.elementRenamed(macrosName)
    }
}