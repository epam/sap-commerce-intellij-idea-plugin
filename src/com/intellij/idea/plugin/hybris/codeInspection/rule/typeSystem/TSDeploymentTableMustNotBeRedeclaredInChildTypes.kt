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

import com.intellij.idea.plugin.hybris.codeInspection.fix.xml.XmlDeleteSubTagQuickFix
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.all
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class TSDeploymentTableMustNotBeRedeclaredInChildTypes : AbstractTSInspection() {

    override fun inspect(
        project: Project,
        dom: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.itemTypes.all.forEach { check(it, project, holder, severity) }
    }

    private fun check(
        dom: ItemType,
        project: Project,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val metaItem = project.service<TSMetaModelStateService>().get().getMetaItem(dom.code.stringValue)
            ?: return

        val currentMetaTypeCode = metaItem.deployment?.typeCode
            ?.takeIf { it.isNotBlank() }
            ?: return

        val metaItemParent = metaItem.allExtends
            .flatMap { it.declarations }
            .firstOrNull { it.deployment?.typeCode?.isNotBlank() ?: false }
            ?: return

        val message = message(
            "hybris.inspections.ts.DeploymentTableMustNotBeRedeclaredInChildTypes.problem.key",
            metaItem.name ?: "?",
            currentMetaTypeCode,
            metaItemParent.name ?: "?",
            metaItemParent.deployment?.typeCode ?: "?"
        )

        holder.createProblem(
            dom,
            severity,
            message,
            getTextRange(dom),
            XmlDeleteSubTagQuickFix(ItemType.DEPLOYMENT)
        )
    }
}