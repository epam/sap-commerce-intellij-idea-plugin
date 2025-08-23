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

package sap.commerce.toolset.project.codeInspection

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import sap.commerce.toolset.codeInspection.fix.XmlDeleteTagQuickFix
import sap.commerce.toolset.i18n
import sap.commerce.toolset.localextensions.model.Extension
import sap.commerce.toolset.localextensions.model.Hybrisconfig
import sap.commerce.toolset.project.settings.ProjectSettings

class LeUnknownExtensionDefinition : LeInspection() {

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
        val hybrisProjectSettings = ProjectSettings.getInstance(project)
        val found = hybrisProjectSettings.availableExtensions.keys
            .firstOrNull { extensionName.equals(it, true) }

        if (found == null) {
            holder.createProblem(
                dom.name,
                severity,
                i18n("hybris.inspections.fix.le.LeUnknownExtensionDeclaration.message", extensionName),
                XmlDeleteTagQuickFix()
            )
        }
    }
}