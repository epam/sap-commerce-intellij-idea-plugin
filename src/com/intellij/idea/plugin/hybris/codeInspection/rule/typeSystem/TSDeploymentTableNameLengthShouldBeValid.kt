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
package com.intellij.idea.plugin.hybris.codeInspection.rule.typeSystem

import com.intellij.idea.plugin.hybris.codeInspection.fix.xml.XmlUpdateAttributeQuickFix
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.system.type.model.Deployment
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.deployments
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class TSDeploymentTableNameLengthShouldBeValid : AbstractCustomOnlyTSInspection() {

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
            ?.findMacroProperty(HybrisConstants.PROPERTY_DEPLOYMENT_TABLENAME_MAXLENGTH)
            ?.value
            ?.toIntOrNull()
            ?: HybrisConstants.DEFAULT_DEPLOYMENT_TABLENAME_MAXLENGTH

        if (tableName.length > maxLength) {
            holder.createProblem(
                dom.table,
                severity,
                message("hybris.inspections.fix.ts.TSDeploymentTableNameLengthShouldBeValid.key", tableName, tableName.length, maxLength),
                XmlUpdateAttributeQuickFix(Deployment.TABLE, tableName.substring(0, maxLength))
            )
        }
    }
}