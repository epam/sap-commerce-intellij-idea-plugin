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
package com.intellij.idea.plugin.hybris.system.bean.lang.folding

import ai.grazie.utils.toDistinctTypedArray
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.model.*
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.util.xml.DomManager
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class BeansXmlFoldingBuilder : FoldingBuilderEx(), DumbAware {

    private val foldHints = "[hints]"
    private val foldEnum = "[enum] "
    private val foldAbstract = "[abstract] "

    private val filter = BeansXmlFilter()
    private val foldTableLikeProperties: Key<Boolean> = Key.create("hybris_fold_table_like_properties")

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!HybrisProjectSettingsComponent.getInstance(root.project).isHybrisProject()) return emptyArray()
        if (root !is XmlFile) return emptyArray()
        DomManager.getDomManager(root.project).getFileElement(root, Beans::class.java)
            ?: return emptyArray()
        val foldingSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(root.project).state.beanSystemSettings.folding

        if (!foldingSettings.enabled) {
            root.removeUserData(foldTableLikeProperties)
            return emptyArray()
        }

        root.putUserData(foldTableLikeProperties, foldingSettings.tableLikeProperties)

        return SyntaxTraverser.psiTraverser(root)
            .filter { filter.isAccepted(it) }
            .mapNotNull {
                if (it is PsiErrorElement || it.textRange.isEmpty) return@mapNotNull null
                FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(GROUP_NAME))
            }
            .toDistinctTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Bean.PROPERTY -> psi.getAttributeValue(Property.NAME)
                ?.let {
                    if (psi.getParentOfType<XmlFile>(false)?.getUserData(foldTableLikeProperties) == true) {
                        val propertyNamePostfix = " ".repeat(getLongestPropertyLength(psi) - it.length)
                        it + propertyNamePostfix
                    } else {
                        it
                    }
                } + " : " +
                (BSMetaHelper.flattenType(psi.getAttributeValue(Property.TYPE)) ?: "?")

            Enum.VALUE -> psi.value.trimmedText
            AbstractPojo.DESCRIPTION -> "-- ${psi.value.trimmedText} --"

            Beans.BEAN -> (psi.getAttributeValue(Bean.ABSTRACT)
                ?.takeIf { "true".equals(it, true) }
                ?.let { foldAbstract }
                ?: "") +
                BSMetaHelper.flattenType(psi.getAttributeValue(AbstractPojo.CLASS)) +
                (BSMetaHelper.flattenType(psi.getAttributeValue(Bean.EXTENDS))
                    ?.let { " : $it" }
                    ?: "")

            Beans.ENUM -> foldEnum + BSMetaHelper.flattenType(psi.getAttributeValue(AbstractPojo.CLASS))

            Hints.HINT -> psi.getAttributeValue(Hint.NAME) +
                (psi.value.trimmedText
                    .takeIf { it.isNotBlank() }
                    ?.let { " : $it" } ?: "")

            Bean.HINTS -> psi.subTags
                .map { it.getAttributeValue(Hint.NAME) }
                .joinToString()
                .takeIf { it.isNotBlank() }
                ?.let { "$foldHints : $it" }
                ?: foldHints

            else -> FALLBACK_PLACEHOLDER
        }

        is XmlToken -> when (val text = psi.text) {
            HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED -> HybrisConstants.BS_SIGN_LESS_THAN
            HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED -> HybrisConstants.BS_SIGN_GREATER_THAN

            else -> text
        }

        else -> FALLBACK_PLACEHOLDER
    }

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Bean.PROPERTY,
            Enum.VALUE,
            AbstractPojo.DESCRIPTION,
            Hints.HINT,
            Bean.HINTS -> true

            else -> false
        }

        is XmlToken -> when (psi.text) {
            HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED,
            HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED -> true

            else -> false
        }

        else -> false
    }

    private fun getLongestPropertyLength(psi: PsiElement) = psi.parent.childrenOfType<XmlTag>()
        .filter { it.localName == Bean.PROPERTY }
        .mapNotNull { it.getAttributeValue(Property.NAME) }
        .maxOfOrNull { it.length }
        ?: 0

    companion object {
        private const val GROUP_NAME = "BeansXml"
        private const val FALLBACK_PLACEHOLDER = "..."
    }

}
