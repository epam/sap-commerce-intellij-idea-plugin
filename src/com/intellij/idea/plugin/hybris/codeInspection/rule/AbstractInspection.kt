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

package com.intellij.idea.plugin.hybris.codeInspection.rule

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.ex.InspectionProfileWrapper
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomFileElement
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomElementAnnotationsManager
import com.intellij.util.xml.highlighting.DomElementsInspection
import com.intellij.util.xml.highlighting.DomHighlightingHelper

abstract class AbstractInspection<T : DomElement>(domClass: Class<T>) : DomElementsInspection<T>(domClass) {

    override fun checkFileElement(domFileElement: DomFileElement<T>, holder: DomElementAnnotationHolder) {
        val file = domFileElement.file
        val project = file.project
        if (!CommonIdeaService.instance.isHybrisProject(project)) return
        if (!canProcess(project, file)) return

        val helper = DomElementAnnotationsManager.getInstance(project).highlightingHelper
        val problemHighlightType = getProblemHighlightType(file)
        val dom = domFileElement.rootElement

        if (!canProcess(dom)) return

        inspect(project, dom, holder, helper, problemHighlightType.severity)
    }

    abstract fun canProcess(project: Project, file: XmlFile): Boolean

    abstract fun inspect(
        project: Project,
        dom: T,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    )

    open fun canProcess(dom: T): Boolean = true

    protected fun getTextRange(dom: DomElement): TextRange? {
        val xmlElement = dom.xmlElement ?: return null

        return TextRange.from(0, xmlElement.textLength)
    }

    protected fun getTextRange(xmlElement: XmlElement?): TextRange? {
        if (xmlElement == null) return null

        return TextRange.from(0, xmlElement.textLength)
    }

    private fun getProblemHighlightType(file: PsiFile): HighlightDisplayLevel {
        val profile = ProjectInspectionProfileManager.getInstance(file.project).currentProfile
        val inspectProfile = InspectionProfileWrapper(profile)
        return inspectProfile.getErrorLevel(HighlightDisplayKey.find(shortName), file)
    }

}