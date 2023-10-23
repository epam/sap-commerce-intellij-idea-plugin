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

package com.intellij.idea.plugin.hybris.codeInspection.rule.beanSystem

import com.intellij.idea.plugin.hybris.codeInspection.fix.xml.XmlAddTagQuickFix
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomFileElement
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomElementAnnotationsManager
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class BSNotWSBeanInspection : AbstractBSInspection() {

    override fun checkFileElement(domFileElement: DomFileElement<Beans>, holder: DomElementAnnotationHolder) {
        val file = domFileElement.file
        val project = file.project
        if (!HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()) return
        if (!canProcess(project, file)) return

        val helper = DomElementAnnotationsManager.getInstance(project).highlightingHelper
        val problemHighlightType = getProblemHighlightType(file)
        val dom = domFileElement.rootElement

        if (!canProcess(dom, project)) return

        inspect(project, dom, holder, helper, problemHighlightType.severity)
    }

    private fun canProcess(dom: Beans, project: Project): Boolean {
        if (!canProcess(dom)) return false

        val beanShortNames = dom.beans
            .mapNotNull { BSMetaModelAccess.getInstance(project).findMetasForDom(it).firstOrNull() }
            .map { it.shortName }
        return BSMetaModelAccess.getInstance(project)
            .getAll<BSGlobalMetaBean>(BSMetaType.META_WS_BEAN)
            .any { globalWsBean -> beanShortNames.any { fileBeanShortNames -> globalWsBean.shortName == fileBeanShortNames } }
    }

    override fun inspect(
        project: Project,
        dom: Beans,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.beans
            .forEach { inspect(it, holder, severity) }
    }

    private fun inspect(
        dom: Bean,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        if (!dom.hints.exists()) {
            holder.createProblem(
                dom,
                severity,
                displayName,
                XmlAddTagQuickFix("hints", "<hint name=\"${HybrisConstants.WS_RELATED}\"/>", null, null)
            )
        } else if (dom.hints.exists() && dom.hints.hints.none { it.name.value == "wsRelated" }) {
            holder.createProblem(
                dom.hints,
                severity,
                displayName,
                XmlAddTagQuickFix("hint", null, null, mapOf("name" to "wsRelated"))
            )
        }
    }
}