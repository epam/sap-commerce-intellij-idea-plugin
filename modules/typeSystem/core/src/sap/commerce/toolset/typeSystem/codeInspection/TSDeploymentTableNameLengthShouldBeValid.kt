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
package sap.commerce.toolset.typeSystem.codeInspection

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.codeInspection.fix.XmlUpdateAttributeQuickFix
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.typeSystem.model.Deployment
import sap.commerce.toolset.typeSystem.model.Items
import sap.commerce.toolset.typeSystem.model.deployments

class TSDeploymentTableNameLengthShouldBeValid : CustomOnlyTSInspection() {

    override fun inspect(
        project: Project,
        dom: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.deployments.forEach { check(it, project, holder, severity) }
    }

    private fun check(
        dom: Deployment,
        project: Project,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val tableName = dom.table.stringValue ?: return
        val maxLength = PropertyService.getInstance(project)
            .findMacroProperty(HybrisConstants.PROPERTY_DEPLOYMENT_TABLENAME_MAXLENGTH)
            ?.value
            ?.toIntOrNull()
            ?: HybrisConstants.DEFAULT_DEPLOYMENT_TABLENAME_MAXLENGTH

        if (tableName.length > maxLength) {
            holder.createProblem(
                dom.table,
                severity,
                i18n("hybris.inspections.fix.ts.TSDeploymentTableNameLengthShouldBeValid.key", tableName, tableName.length, maxLength),
                XmlUpdateAttributeQuickFix(Deployment.TABLE, tableName.substring(0, maxLength))
            )
        }
    }
}