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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCustomPropertyService
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty
import com.intellij.idea.plugin.hybris.type.system.model.ItemType
import com.intellij.idea.plugin.hybris.type.system.model.Items
import com.intellij.idea.plugin.hybris.type.system.model.stream
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class CatalogAwareUniqueKeyAttributeQualifier : AbstractTypeSystemInspection() {

    override fun checkItems(
        project: Project,
        items: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        items.itemTypes.stream
            .forEach { check(it, holder, severity, project) }
    }

    private fun check(
        dom: ItemType,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        val meta = TSMetaModelAccess.getInstance(project).getMetaModel().getMetaItem(dom.code.stringValue)
            ?: return
        val domCustomProperty = dom.customProperties.properties
            .first { TSMetaCustomProperty.KnownProperties.UNIQUE_KEY_ATTRIBUTE_QUALIFIER.equals(it.name.stringValue, true) }
            ?: return
        val customPropertyValue = TSMetaCustomPropertyService.getInstance(project).parseCommaSeparatedStringValue(domCustomProperty)
            ?: return

        val metaItemService = TSMetaItemService.getInstance(project)
        val nonUniqueQualifiers = customPropertyValue
            .filter {qualifier ->
                val attributes = metaItemService.findAttributesByName(meta, qualifier, true).map { it.modifiers }
                val referenceEnds = metaItemService.findReferenceEndsByRole(meta, qualifier, true).map { it.modifiers }

                (attributes + referenceEnds).none { it.isUnique }
            }

        if (nonUniqueQualifiers.isNotEmpty()) {
            holder.createProblem(
                domCustomProperty,
                severity,
                displayName,
                getTextRange(dom)
            )
        }
    }
}