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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.type.system.model.Items
import com.intellij.idea.plugin.hybris.type.system.model.all
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class TypeNameMustPointToExistingType : AbstractTSInspection() {

    override fun inspect(
        project: Project,
        items: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        val collectionElementTypes = items.collectionTypes.collectionTypes
            .map { it.elementType }
        val mapArgumentTypes = items.mapTypes.mapTypes
            .flatMap { listOf(it.argumentType, it.returnType) }
        val attributeTypes = items.itemTypes.all
            .flatMap { it.attributes.attributes }
            .map { it.type }

        (collectionElementTypes + mapArgumentTypes + attributeTypes)
            .forEach { check(it, holder, severity, project) }
    }

    private fun check(
        dom: GenericAttributeValue<String>,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        val typeCode = dom.stringValue
            ?.replace(HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX, "")
            ?: return

        // If type code is Primitive - skip, it is not registered via TS, but available in Service Layer
        if (HybrisConstants.TS_PRIMITIVE_TYPES.contains(typeCode)) return

        val metaModel = TSMetaModelAccess.getInstance(project).getMetaModel()

        val meta = metaModel.getMetaAtomic(typeCode)
            ?: metaModel.getMetaEnum(typeCode)
            ?: metaModel.getMetaCollection(typeCode)
            ?: metaModel.getMetaRelation(typeCode)
            ?: metaModel.getMetaMap(typeCode)
            ?: metaModel.getMetaItem(typeCode)

        if (meta == null) {
            holder.createProblem(
                dom,
                severity,
                displayName
            )
        }
    }
}