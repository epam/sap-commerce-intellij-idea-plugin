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

package com.intellij.idea.plugin.hybris.codeInspection.rule.localextensions

import com.intellij.idea.plugin.hybris.codeInspection.fix.XmlDeleteTagQuickFix
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.localextensions.model.Extension
import com.intellij.idea.plugin.hybris.system.localextensions.model.Hybrisconfig
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class LeUnknownExtensionDefinition : AbstractLeInspection() {

    override fun inspect(
        project: Project,
        dom: Hybrisconfig,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.extensions.extensions
            .filterNotNull()
            .forEach { inspect(it, holder, severity, project) }
    }

    private fun inspect(
        dom: Extension,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        val extensionName = dom.name.stringValue ?: return
        val hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project)
        val found = hybrisProjectSettings.getAvailableExtensions().keys
            .firstOrNull { extensionName.equals(it, true) }

        if (found == null) {
            holder.createProblem(
                dom.name,
                severity,
                message("hybris.inspections.fix.le.LeUnknownExtensionDeclaration.message", extensionName),
                XmlDeleteTagQuickFix()
            )
        }
    }
}