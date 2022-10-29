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

package com.intellij.idea.plugin.hybris.type.system.inspections.rules

import com.intellij.idea.plugin.hybris.type.system.inspections.fix.XmlAddTagQuickFix
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.type.system.meta.model.MetaType
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.type.system.model.Deployment
import com.intellij.idea.plugin.hybris.type.system.model.ItemType
import com.intellij.idea.plugin.hybris.type.system.model.Items
import com.intellij.idea.plugin.hybris.type.system.model.stream
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import org.apache.commons.lang3.StringUtils

class DeploymentTableMustExistForItemExtendingGenericItem : AbstractTypeSystemInspection() {

    override fun checkItems(
        project: Project,
        items: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        items.itemTypes.stream.forEach { check(it, project, holder, severity) }
    }

    private fun check(
        dom: ItemType,
        project: Project,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        // skip non-ComposedType
        if ("ViewType".equals(dom.metaType.stringValue, true) || "ComposedType".equals(dom.metaType.stringValue, true)) return

        val metaItem = TSMetaModelAccess.getInstance(project).getMetaModel().getMetaType<TSGlobalMetaItem>(MetaType.META_ITEM)[dom.code.stringValue]
            ?: return

        if (StringUtils.isNotBlank(metaItem.deployment.typeCode)) return

        val otherDeclarationsWithDeploymentTable = metaItem.declarations.any { StringUtils.isNotBlank(it.deployment.typeCode) }

        if (otherDeclarationsWithDeploymentTable) return

        val otherDeclarationsMarkedAsAbstract = metaItem.declarations.any { it.isAbstract }

        if (metaItem.isAbstract || otherDeclarationsMarkedAsAbstract) return

        val allExtends = TSMetaItemService.getInstance(project).getExtends(metaItem)
            .flatMap { it.declarations }

        // skip Descriptor declarations
        if (allExtends.count { "Descriptor".equals(it.name, true) } > 0) return

        val countDeploymentTablesInParents = allExtends
            .count { StringUtils.isNotBlank(it.deployment.typeCode) }

        if (countDeploymentTablesInParents > 0) return

        val attributes = sortedMapOf(
            Deployment.TABLE to dom.code.stringValue,
            Deployment.TYPE_CODE to TSMetaModelAccess.getInstance(project).getMetaModel().getNextAvailableTypeCode().toString(),
        )

        holder.createProblem(
            dom,
            severity,
            displayName,
            getTextRange(dom),
            XmlAddTagQuickFix(ItemType.DEPLOYMENT, null, attributes, ItemType.DESCRIPTION)
        )
    }
}