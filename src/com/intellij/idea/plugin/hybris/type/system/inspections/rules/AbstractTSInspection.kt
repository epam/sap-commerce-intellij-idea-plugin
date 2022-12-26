/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.type.system.inspections.rules

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.ex.InspectionProfileWrapper
import com.intellij.idea.plugin.hybris.type.system.model.Items
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlElement
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomFileElement
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomElementAnnotationsManager
import com.intellij.util.xml.highlighting.DomElementsInspection
import com.intellij.util.xml.highlighting.DomHighlightingHelper

abstract class AbstractTSInspection : DomElementsInspection<Items>(Items::class.java) {

    override fun checkFileElement(domFileElement: DomFileElement<Items>, holder: DomElementAnnotationHolder) {
        val helper = DomElementAnnotationsManager.getInstance(domFileElement.manager.project).highlightingHelper
        val problemHighlightType = getProblemHighlightType(domFileElement.file)
        val project = domFileElement.file.project

        inspect(project, domFileElement.rootElement, holder, helper, problemHighlightType.severity)
    }

    abstract fun inspect(
        project: Project,
        items: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    )

    protected fun getTextRange(dom : DomElement): TextRange? {
        val xmlElement = dom.xmlElement ?: return null

        return TextRange.from(0, xmlElement.textLength)
    }

    protected fun getTextRange(xmlElement : XmlElement?): TextRange? {
        if (xmlElement == null) return null

        return TextRange.from(0, xmlElement.textLength)
    }

    private fun getProblemHighlightType(file: PsiFile): HighlightDisplayLevel {
        val profile = ProjectInspectionProfileManager.getInstance(file.project).currentProfile
        val inspectProfile = InspectionProfileWrapper(profile)
        return inspectProfile.getErrorLevel(HighlightDisplayKey.find(shortName), file)
    }

}