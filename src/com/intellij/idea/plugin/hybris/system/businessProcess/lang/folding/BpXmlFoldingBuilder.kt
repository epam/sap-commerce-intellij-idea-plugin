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
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager
import org.jetbrains.kotlin.idea.gradleTooling.get

class BpXmlFoldingBuilder : FoldingBuilderEx(), DumbAware {

    val foldEnd = "[end] "
    val foldChoice = "[choice] "
    val foldTimeout = "[timeout] wait for "
    val foldCase = "[case] "
    val foldEvent = "[event] "
    val foldNA = "n/a"

    private val filter = BpXmlFilter()

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
            Action.TRANSITION -> fold(psi, Transition.NAME, Transition.TO)

            Process.END -> fold(psi, NavigableElement.ID, End.STATE, foldEnd)

            Case.CHOICE -> fold(psi, NavigableElement.ID, Choice.THEN, foldChoice)

            Wait.TIMEOUT -> foldTimeout +
                BpHelper.parseDuration(psi.getAttributeValue(Timeout.DELAY) ?: "?") +
                " then " +
                psi.getAttributeValue(Timeout.THEN)

            Wait.CASE -> foldCase +
                psi.getAttributeValue(Case.EVENT) +
                " : " +
                (psi.subTags
                    .map { it.getAttributeValue(NavigableElement.ID) }
                    .joinToString()
                    .takeIf { it.isNotBlank() } ?: foldNA)

            Wait.EVENT -> foldEvent + psi.value.trimmedText

            Process.ACTION -> "[action] #" +
                psi.getAttributeValue(NavigableElement.ID) +
                " --> [bean] #" +
                psi.getAttributeValue(Action.BEAN)

            Process.WAIT -> {
                val events = psi.subTags
                    .find { it.localName == Wait.EVENT }
                    ?.value
                    ?.let { listOf(it.trimmedText) } ?: psi.subTags
                    .filter { it.localName == Wait.CASE }
                    .mapNotNull { it.getAttributeValue(Case.EVENT) }
                val eventsNames = events.joinToString()
                val s = if (events.size > 1) "s" else ""

                psi.subTags
                    .find { it.localName == Wait.TIMEOUT }
                    ?.getAttributeValue(Timeout.DELAY)
                    ?.let { BpHelper.parseDuration(it) }
                    ?.let {
                        "[wait] #" +
                            psi.getAttributeValue(NavigableElement.ID) +
                            " for the event$s for $it: " +
                            eventsNames
                    }
                    ?: FALLBACK_PLACEHOLDER
            }

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    private fun fold(psi: XmlTag, attr1: String, attr2: String, prefix: String = "") = prefix +
        psi.getAttributeValue(attr1) +
        " -> " +
        psi.getAttributeValue(attr2)

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

    companion object {
        private const val GROUP_NAME = "BusinessProcessXml"
        private const val FALLBACK_PLACEHOLDER = "..."
    }
}