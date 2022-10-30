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

class CatalogAwareCatalogVersionAttributeQualifier : AbstractTypeSystemInspection() {

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
        val metaModel = TSMetaModelAccess.getInstance(project).getMetaModel()

        val meta = metaModel.getMetaItem(dom.code.stringValue)
            ?: return
        val domCustomProperty = dom.customProperties.properties
            .firstOrNull { TSMetaCustomProperty.KnownProperties.CATALOG_VERSION_ATTRIBUTE_QUALIFIER.equals(it.name.stringValue, true) }
            ?: return

        val qualifier = TSMetaCustomPropertyService.getInstance(project).parseStringValue(domCustomProperty)
            ?: return

        val metaItemService = TSMetaItemService.getInstance(project)
        val attributes = metaItemService.findAttributesByName(meta, qualifier, true)

        val isAttributeTypeCatalogAware = attributes
            .any { attribute ->
                "CatalogVersion".equals(attribute.type, true)
                || metaModel.getMetaItem(attribute.type)?.let { attributeTypeMeta ->
                    metaItemService.getExtends(attributeTypeMeta)
                        .mapNotNull { it.name }
                        .any { "CatalogVersion".equals(it, true) }
                } ?: false
            }

        if (!isAttributeTypeCatalogAware) {
            holder.createProblem(
                domCustomProperty.value,
                severity,
                displayName
            )
        }
    }
}