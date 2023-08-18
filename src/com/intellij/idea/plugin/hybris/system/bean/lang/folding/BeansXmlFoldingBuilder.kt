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
package com.intellij.idea.plugin.hybris.system.bean.lang.folding

import ai.grazie.utils.toDistinctTypedArray
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.idea.plugin.hybris.system.bean.model.Property
import com.intellij.idea.plugin.hybris.system.type.model.*
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

class BeansXmlFoldingBuilder : FoldingBuilderEx(), DumbAware {

    private val filter = BeansXmlFilter()

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!HybrisProjectSettingsComponent.getInstance(root.project).isHybrisProject()) return emptyArray()
        if (root !is XmlFile) return emptyArray()
        DomManager.getDomManager(root.project).getFileElement(root, Beans::class.java)
            ?: return emptyArray()

        return SyntaxTraverser.psiTraverser(root)
            .filter { filter.isAccepted(it) }
            .mapNotNull {
                if (it is PsiErrorElement || it.textRange.isEmpty) return@mapNotNull null
                FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(GROUP_NAME))
            }
            .toDistinctTypedArray()
    }

    // property : Type
    // name       type
    override fun getPlaceholderText(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Bean.PROPERTY -> psi.getAttributeValue(Property.NAME) + " : " +
                (BSMetaHelper.flattenType(psi.getAttributeValue(Property.TYPE)) ?: "?")

            Enum.VALUE -> psi.value.trimmedText
            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Bean.PROPERTY,
            Enum.VALUE -> true

            else -> false
        }

        else -> false
    }

    companion object {
        private const val GROUP_NAME = "BeansXml"
        private const val FALLBACK_PLACEHOLDER = "..."
    }

}
