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

package com.intellij.idea.plugin.hybris.lang.folding

import ai.grazie.utils.toDistinctTypedArray
import com.intellij.idea.plugin.hybris.settings.FoldingSettings
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

abstract class AbstractXmlFoldingBuilderEx<S : FoldingSettings, T : DomElement>(private val clazz: Class<T>) : FoldingBuilderEx() {

    internal abstract val filter: PsiElementFilter
    private val groupName: String = clazz.simpleName + "_Xml"
    private val cachedFoldingSettings: Key<S> = Key.create("hybris_folding_settings_" + clazz.simpleName)

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!HybrisProjectSettingsComponent.getInstance(root.project).isHybrisProject()) return emptyArray()
        if (root !is XmlFile) return emptyArray()
        DomManager.getDomManager(root.project).getFileElement(root, clazz)
            ?: return emptyArray()
        val foldingSettings = initSettings(root.project)

        if (!foldingSettings.enabled) {
            root.removeUserData(cachedFoldingSettings)
            return emptyArray()
        }

        root.putUserData(cachedFoldingSettings, foldingSettings)

        return SyntaxTraverser.psiTraverser(root)
            .filter { filter.isAccepted(it) }
            .mapNotNull {
                if (it is PsiErrorElement || it.textRange.isEmpty) return@mapNotNull null
                FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(groupName))
            }
            .toDistinctTypedArray()
    }

    internal abstract fun initSettings(project: Project): S
    internal fun getCachedFoldingSettings(psi: PsiElement) = psi.getParentOfType<XmlFile>(false)
        ?.getUserData(cachedFoldingSettings)

    fun tablify(psi: PsiElement, value: String, tablify: Boolean?, tagName: String, attributeName: String, prepend: Boolean = false) = if (tablify == true) {
        val propertyNamePostfix = " ".repeat(getLongestLength(psi, tagName, attributeName, value.length) - value.length)
        if (prepend) propertyNamePostfix + value else value + propertyNamePostfix
    } else {
        value
    }

    private fun getLongestLength(psi: PsiElement, tagName: String, attributeName: String, fallbackLength: Int) = psi.parent.childrenOfType<XmlTag>()
        .filter { it.localName == tagName }
        .mapNotNull { it.getAttributeValue(attributeName) }
        .maxOfOrNull { it.length }
        ?: fallbackLength

    companion object {
        internal const val FALLBACK_PLACEHOLDER = "..."
        internal const val TYPE_SEPARATOR = " : "
    }
}