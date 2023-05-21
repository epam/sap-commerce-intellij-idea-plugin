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

package com.intellij.idea.plugin.hybris.codeInspection.rule.typeSystem

import com.intellij.idea.plugin.hybris.codeInspection.fix.xml.XmlDeleteSubTagQuickFix
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.all
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import org.apache.commons.lang3.StringUtils

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
        val metaItem = TSMetaModelAccess.getInstance(project).getMetaModel().getMetaItem(dom.code.stringValue)
            ?: return

        val currentMetaTypeCode = metaItem.deployment?.typeCode

        if (StringUtils.isBlank(currentMetaTypeCode)) return

        val countDeploymentTablesInParents = metaItem.allExtends
            .flatMap { it.declarations }
            .count { StringUtils.isNotBlank(it.deployment?.typeCode) }

        if (countDeploymentTablesInParents == 0) return

        holder.createProblem(
            dom,
            severity,
            displayName,
            getTextRange(dom),
            XmlDeleteSubTagQuickFix(ItemType.DEPLOYMENT)
        )
    }
}