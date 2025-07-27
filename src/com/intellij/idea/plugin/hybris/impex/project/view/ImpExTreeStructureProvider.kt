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
package com.intellij.idea.plugin.hybris.impex.project.view

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.annotations.Unmodifiable

/**
 * This provider enables:
 *  - grouping of the localized ImpEx files similar to ResourceBundle
 */
class ImpExTreeStructureProvider : TreeStructureProvider {

    private val separatorRegex = "_".toRegex()

    override fun modify(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>,
        settings: ViewSettings
    ): @Unmodifiable Collection<AbstractTreeNode<*>> {
        if (!DeveloperSettingsComponent.getInstance(parent.project).state.impexSettings.groupLocalizedFiles) return children
        if (parent is LocalizedImpExNode) return children
        if (children.isEmpty()) return children

        val localizedImpExNodes by lazy { mutableMapOf<String, MutableCollection<AbstractTreeNode<*>>>() }
        val newChildren = mutableListOf<AbstractTreeNode<*>>()

        children.forEach { childNode ->
            val nodeValue = childNode.value

            if (nodeValue is ImpexFile && childNode is PsiFileNode) {
                val baseName = baseName(nodeValue)

                localizedImpExNodes.getOrPut(baseName) { mutableListOf() }
                    .add(childNode)
            } else {
                newChildren.add(childNode)
            }
        }

        localizedImpExNodes.forEach { (baseName, localizedNodes) ->
            if (localizedNodes.size == 1) newChildren.add(localizedNodes.first())
            else newChildren.add(LocalizedImpExNode(baseName, parent.project, localizedNodes, settings))
        }

        return newChildren
    }

    private fun baseName(file: PsiFile): String = CachedValuesManager.getCachedValue(file) {
        fun compute(file: PsiFile): String {
            val name = file.name

            if (!name.contains('_')) return FileUtilRt.getNameWithoutExtension(name)

            val matcher = HybrisConstants.Locales.LOCALIZED_FILE_NAME.matcher(name)
            val baseNameWithExtension: String

            var matchIndex = 0
            while (matcher.find(matchIndex)) {
                val matchResult = matcher.toMatchResult()
                val splitted = matchResult.group(1)
                    .split(separatorRegex)
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (splitted.size > 1) {
                    val langCode: String? = splitted[1]
                    if (!HybrisConstants.Locales.LOCALES_CODES.contains(langCode)) {
                        matchIndex = matchResult.start(1) + 1
                        continue
                    }
                    baseNameWithExtension = name.substring(0, matchResult.start(1)) + name.substring(matchResult.end(1))
                    return FileUtilRt.getNameWithoutExtension(baseNameWithExtension)
                }
            }
            baseNameWithExtension = name
            return FileUtilRt.getNameWithoutExtension(baseNameWithExtension)
        }

        CachedValueProvider.Result.create(compute(file), file)
    }
}
