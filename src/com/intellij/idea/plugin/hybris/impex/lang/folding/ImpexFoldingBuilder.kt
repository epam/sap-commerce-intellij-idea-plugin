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
package com.intellij.idea.plugin.hybris.impex.lang.folding

import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser

class ImpexFoldingBuilder : AbstractImpExFoldingBuilder() {

    override fun buildFoldRegionsInternal(psi: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val filter = ImpExPsiElementFilterFactory.getPsiElementFilter(psi.project)

        val psiElements = SyntaxTraverser.psiTraverser(psi)
            .filter { filter.isAccepted(it) }
            .toList()

        var currentLineGroup = FoldingGroup.newGroup(GROUP_NAME)

        /* Avoid spawning a lot of unnecessary objects for each line break. */
        var groupIsNotFresh = false
        val descriptors = mutableListOf<FoldingDescriptor>()
        for (psiElement in psiElements) {
            if (ImpexPsiUtils.isLineBreak(psiElement)) {
                if (groupIsNotFresh) {
                    currentLineGroup = FoldingGroup.newGroup(GROUP_NAME)
                    groupIsNotFresh = false
                }
            } else {
                descriptors.add(FoldingDescriptor(psiElement.node, psiElement.textRange, currentLineGroup))
                groupIsNotFresh = true
            }
        }
        return descriptors.toTypedArray<FoldingDescriptor>()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val psi = node.psi
        val text = ImpexFoldingPlaceholderBuilderFactory.getPlaceholderBuilder(psi.project).getPlaceholder(psi)
        var resolvedMacro = text
        if (text.startsWith("$")) {
            val cache = ImpexMacroUtils.getFileCache(psi.containingFile).value
            val descriptor = cache[text]
            if (descriptor != null) {
                resolvedMacro = descriptor.resolvedValue
            }
        }
        return if (resolvedMacro.length <= text.length) {
            resolvedMacro
        } else text
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }

    companion object {
        private const val GROUP_NAME = "ImpEx"
    }
}
