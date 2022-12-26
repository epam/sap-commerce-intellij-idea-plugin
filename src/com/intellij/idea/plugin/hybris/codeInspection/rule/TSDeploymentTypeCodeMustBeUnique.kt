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

import com.intellij.idea.plugin.hybris.codeInspection.fix.XmlUpdateAttributeQuickFix
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.Deployment
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.deployments
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import org.apache.commons.lang3.StringUtils

class TSDeploymentTypeCodeMustBeUnique : AbstractTSInspection() {

    override fun inspect(
        project: Project,
        items: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        items.deployments.forEach { check(it, project, holder, severity) }
    }

    private fun check(
        dom: Deployment,
        project: Project,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val deployment = TSMetaModelAccess.getInstance(project).getMetaModel().getDeploymentForTypeCode(dom.typeCode.stringValue)
        deployment ?: return

        if (StringUtils.equals(dom.table.stringValue, deployment.table)) return

        holder.createProblem(
            dom.typeCode,
            severity,
            displayName,
            XmlUpdateAttributeQuickFix(
                Deployment.TYPE_CODE,
                TSMetaModelAccess.getInstance(project).getMetaModel().getNextAvailableTypeCode().toString()
            )
        )
    }
}