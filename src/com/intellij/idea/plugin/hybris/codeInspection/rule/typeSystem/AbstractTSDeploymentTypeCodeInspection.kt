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

package com.intellij.idea.plugin.hybris.codeInspection.rule.typeSystem

import com.intellij.idea.plugin.hybris.codeInspection.fix.xml.XmlUpdateAttributeQuickFix
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaDeployment
import com.intellij.idea.plugin.hybris.system.type.model.Deployment
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.deployments
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

abstract class AbstractTSDeploymentTypeCodeInspection : AbstractCustomOnlyTSInspection() {

    protected open fun applicable(project: Project, dom: Deployment): Boolean = true
    protected open fun applicable(project: Project, dom: Deployment, deployment: TSMetaDeployment): Boolean = true
    protected open fun customMessage(project: Project, dom: Deployment): String? = null

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
        if (!applicable(project, dom)) return

        val deployment = project.service<TSMetaModelStateService>().get()
            .getDeploymentForTypeCode(dom.typeCode.stringValue)
        deployment ?: return

        if (!applicable(project, dom, deployment)) return

        val fix = TSMetaModelAccess.getInstance(project).getNextAvailableTypeCode()
            ?.toString()
            ?.let {
                XmlUpdateAttributeQuickFix(
                    Deployment.TYPE_CODE,
                    it
                )
            }
        val message = customMessage(project, dom) ?: displayName
        if (fix == null) {
            holder.createProblem(dom.typeCode, severity, message)
        } else {
            holder.createProblem(dom.typeCode, severity, message, fix)
        }
    }
}