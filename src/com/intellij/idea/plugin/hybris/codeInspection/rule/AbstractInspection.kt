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

package com.intellij.idea.plugin.hybris.codeInspection.rule

import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.ex.InspectionProfileWrapper
import com.intellij.idea.plugin.hybris.util.isNotHybrisProject
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
        if (project.isNotHybrisProject) return
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

    protected fun getTextRange(dom: DomElement) = dom.xmlElement
        ?.let { TextRange.from(0, it.textLength) }

    protected fun getTextRange(xmlElement: XmlElement?) = xmlElement
        ?.let { TextRange.from(0, it.textLength) }

    private fun getProblemHighlightType(file: PsiFile) = HighlightDisplayKey.find(shortName)
        ?.let {
            val profile = ProjectInspectionProfileManager.getInstance(file.project).currentProfile
            InspectionProfileWrapper(profile).getErrorLevel(it, file)
        }
        ?: defaultLevel

}