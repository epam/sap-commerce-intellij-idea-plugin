/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.cockpitng.lang.folding

import ai.grazie.utils.toDistinctTypedArray
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.cockpitng.model.advancedSearch.Field
import com.intellij.idea.plugin.hybris.system.cockpitng.model.advancedSearch.FieldList
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.itemEditor.Attribute
import com.intellij.idea.plugin.hybris.system.cockpitng.model.itemEditor.Section
import com.intellij.idea.plugin.hybris.system.cockpitng.model.listView.ListColumn
import com.intellij.idea.plugin.hybris.system.cockpitng.model.listView.ListView
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.ExplorerTree
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.TypeNode
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager

class CngConfigFoldingBuilder : FoldingBuilderEx(), DumbAware {

    private val filter = CngConfigFilter()

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!HybrisProjectSettingsComponent.getInstance(root.project).isHybrisProject()) return emptyArray()
        if (root !is XmlFile) return emptyArray()
        DomManager.getDomManager(root.project).getFileElement(root, Config::class.java)
            ?: return emptyArray()

        return SyntaxTraverser.psiTraverser(root)
            .filter { filter.isAccepted(it) }
            .mapNotNull {
                if (it is PsiErrorElement || it.textRange.isEmpty) return@mapNotNull null
                FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(GROUP_NAME))
            }
            .toDistinctTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            FieldList.FIELD -> psi.getAttributeValue(Field.NAME)
            ListView.COLUMN -> psi.getAttributeValue(ListColumn.QUALIFIER)

            Section.ATTRIBUTE -> psi.getAttributeValue(Attribute.QUALIFIER) + (
                psi.getAttributeValue(Attribute.READONLY)
                    ?.takeIf { "true".equals(it, true) }
                    ?.let { " : readonly" }
                    ?: ""
                ) + (
                psi.getAttributeValue(Attribute.VISIBLE)
                    ?.takeIf { "false".equals(it, true) }
                    ?.let { " : non-visible" }
                    ?: ""
                )

            ExplorerTree.TYPE_NODE -> psi.getAttributeValue(TypeNode.ID) + (
                psi.getAttributeValue(TypeNode.CODE)
                    ?.let { " : $it" }
                    ?: ""
                )

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            FieldList.FIELD,
            Section.ATTRIBUTE,
            ExplorerTree.TYPE_NODE,
            ListView.COLUMN -> true

            else -> false
        }

        else -> false
    }

    companion object {
        private const val GROUP_NAME = "CngConfigXml"
        private const val FALLBACK_PLACEHOLDER = "..."
    }

}
