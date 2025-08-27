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

package sap.commerce.toolset.businessProcess.lang.folding

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.xml.XmlTag
import sap.commerce.toolset.businessProcess.model.*
import sap.commerce.toolset.businessProcess.settings.BpFoldingSettings
import sap.commerce.toolset.businessProcess.settings.state.BpFoldingSettingsState
import sap.commerce.toolset.businessProcess.util.BpHelper
import sap.commerce.toolset.folding.XmlFoldingBuilderEx

class BpXmlFoldingBuilder : XmlFoldingBuilderEx<BpFoldingSettingsState, Process>(Process::class.java), DumbAware {

    override val filter = PsiElementFilter {
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

    override fun initSettings(project: Project) = BpFoldingSettings.getInstance().state

    override fun getPlaceholderText(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Action.TRANSITION -> fold(psi, Transition.NAME, Transition.TO, Action.TRANSITION, tablify = getCachedFoldingSettings(psi)?.tablifyActionTransitions)

            Process.END -> fold(psi, NavigableElement.ID, End.STATE, Process.END, "[end]    ", tablify = getCachedFoldingSettings(psi)?.tablifyEnds)

            Case.CHOICE -> fold(psi, NavigableElement.ID, Choice.THEN, Case.CHOICE, "[choice] ", tablify = getCachedFoldingSettings(psi)?.tablifyCaseChoices)

            Wait.TIMEOUT -> "[timeout] wait for " +
                BpHelper.parseDuration(psi.getAttributeValue(Timeout.DELAY) ?: "?") +
                " then " +
                psi.getAttributeValue(Timeout.THEN)

            Wait.CASE -> "[case] " +
                psi.getAttributeValue(Case.EVENT) +
                TYPE_SEPARATOR +
                (psi.subTags
                    .map { it.getAttributeValue(NavigableElement.ID) }
                    .joinToString()
                    .takeIf { it.isNotBlank() }
                    ?: "n/a")

            Wait.EVENT -> "[event] " + psi.value.trimmedText

            Process.ACTION -> "[action] " +
                psi.getAttributeValue(NavigableElement.ID)

            Process.WAIT -> "[wait]   " +
                psi.getAttributeValue(NavigableElement.ID)

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    private fun fold(psi: XmlTag, attr1: String, attr2: String, tagName: String, prefix: String = "", tablify: Boolean?) = prefix +
        psi.getAttributeValue(attr1)
            ?.let { tablify(psi, it, tablify, tagName, attr1) } +
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
}