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

import com.intellij.idea.plugin.hybris.type.system.meta.MetaType
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.type.system.model.ItemType
import com.intellij.idea.plugin.hybris.type.system.model.Items
import com.intellij.idea.plugin.hybris.type.system.model.stream
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import java.util.stream.Collectors

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
        it: ItemType,
        project: Project,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val metaItem = TSMetaModelAccess.getInstance(project).getMetaModel().getMetaType<TSMetaItem>(MetaType.META_ITEM)[it.code.stringValue]
            ?: return

        val isAbstract = metaItem.retrieveAllDomsStream()
            .filter { it.abstract.exists() }
            .map { it.abstract.value }
            .count()

        if (isAbstract > 0) {
            return
        }

        val countExtends = TSMetaItemService.getInstance(project).getExtends(metaItem)
            .flatMap { it.retrieveAllDomsStream().collect(Collectors.toList()) }
            .map { it.deployment }
            .filter { it.exists() }
            .count()

        val countOtherDeclarations = metaItem.retrieveAllDomsStream()
            .map { it.deployment }
            .filter { it.exists() }
            .count()

        if (countExtends == 0 && (!it.deployment.exists() && countOtherDeclarations == 0L)) {
            holder.createProblem(it, severity, displayName, getTextRange(it))
        }
    }
}