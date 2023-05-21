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

package com.intellij.idea.plugin.hybris.codeInspection.rule.extensioninfo

import com.intellij.idea.plugin.hybris.codeInspection.fix.xml.XmlDeleteTagQuickFix
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.extensioninfo.model.ExtensionInfo
import com.intellij.idea.plugin.hybris.system.extensioninfo.model.RequiresExtension
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class EiDuplicateExtensionDefinition : AbstractEiInspection() {

    override fun inspect(
        project: Project,
        dom: ExtensionInfo,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        val declaredDependencies = dom.extension.requiresExtensions
            .filter { it.name.stringValue != null }
            .groupBy { it.name.stringValue!!.lowercase() }

        dom.extension.requiresExtensions
            .filter { it.name.stringValue != null }
            .filter { declaredDependencies.getOrDefault(it.name.stringValue!!.lowercase(), emptyList()).size > 1 }
            .forEach { createProblem(it, holder, severity) }
    }

    private fun createProblem(
        dom: RequiresExtension,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) = holder.createProblem(
        dom.name,
        severity,
        message("hybris.inspections.fix.ei.EiDuplicateExtensionDeclaration.message", dom.name.stringValue!!),
        XmlDeleteTagQuickFix()
    )
}