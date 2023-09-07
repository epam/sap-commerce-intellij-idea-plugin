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

package com.intellij.idea.plugin.hybris.system.businessProcess.lang.folding

import ai.grazie.utils.toDistinctTypedArray
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.businessProcess.model.*
import com.intellij.idea.plugin.hybris.system.businessProcess.util.BpHelper
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager




class BpXmlFoldingBuilder : FoldingBuilderEx(), DumbAware {

    private val foldEnd = "[end]    "
    private val foldChoice = "[choice] "
    private val foldTimeout = "[timeout] wait for "
    private val foldCase = "[case] "
    private val foldEvent = "[event] "
    private val foldNA = "n/a"
    private val actionPrefix = "[action] "
    private val waitPrefix = "[wait]   "
    private val delimiter = " : "
    private val then = " then "
    private val arrowDelimiter = " -> "

    private val filter = PsiElementFilter {
        when (it) {
            is XmlTag -> when (it.localName) {
                Action.TRANSITION,
                Process.END,
                Case.CHOICE,
                Wait.CASE,
                Wait.TIMEOUT,
                Wait.EVENT,
                Process.ACTION,
                Process.WAIT -> true

                else -> false
            }

            else -> false
        }
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!HybrisProjectSettingsComponent.getInstance(root.project).isHybrisProject()) return emptyArray()
        if (root !is XmlFile) return emptyArray()
        DomManager.getDomManager(root.project).getFileElement(root, Process::class.java)
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
            Action.TRANSITION -> fold(psi, Transition.NAME, Transition.TO, Action.TRANSITION)

            Process.END -> fold(psi, NavigableElement.ID, End.STATE, Process.END, foldEnd)

            Case.CHOICE -> fold(psi, NavigableElement.ID, Choice.THEN, Case.CHOICE, foldChoice)

            Wait.TIMEOUT -> foldTimeout +
                BpHelper.parseDuration(psi.getAttributeValue(Timeout.DELAY) ?: "?") +
                then +
                psi.getAttributeValue(Timeout.THEN)

            Wait.CASE -> foldCase +
                psi.getAttributeValue(Case.EVENT) +
                delimiter +
                (psi.subTags
                    .map { it.getAttributeValue(NavigableElement.ID) }
                    .joinToString()
                    .takeIf { it.isNotBlank() } ?: foldNA)

            Wait.EVENT -> foldEvent + psi.value.trimmedText

            Process.ACTION -> actionPrefix +
                psi.getAttributeValue(NavigableElement.ID)

            Process.WAIT -> waitPrefix +
                psi.getAttributeValue(NavigableElement.ID)

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    private fun fold(psi: XmlTag, attr1: String, attr2: String, tagName: String, prefix: String = "") = prefix +
        psi.getAttributeValue(attr1)
            ?.let { tablify(psi, it, true, tagName, attr1) } +
        arrowDelimiter +
        psi.getAttributeValue(attr2)
            ?.let { tablify(psi, it, true, tagName, attr2) }

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Action.TRANSITION,
            Process.END,
            Case.CHOICE,
            Wait.TIMEOUT,
            Wait.EVENT -> true

            else -> false
        }

        else -> false
    }

    private fun tablify(psi: PsiElement, value: String, tablify: Boolean?, tagName: String, attributeName: String, prepend: Boolean = false) = if (tablify == true) {
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
        private const val GROUP_NAME = "BusinessProcessXml"
        private const val FALLBACK_PLACEHOLDER = "..."
    }
}