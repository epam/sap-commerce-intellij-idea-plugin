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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaHelper
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.all
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class TSCatalogAwareCatalogVersionAttributeQualifier : AbstractTSInspection() {

    override fun inspect(
        project: Project,
        dom: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.itemTypes.all.forEach { check(it, holder, severity, project) }
    }

    private fun check(
        dom: ItemType,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        val metaModel = TSMetaModelStateService.state(project)

        val meta = metaModel.getMetaItem(dom.code.stringValue)
            ?: return
        val domCustomProperty = TSMetaHelper.getProperty(dom.customProperties, HybrisConstants.TS_CATALOG_VERSION_ATTRIBUTE_QUALIFIER)
            ?: return
        val qualifier = TSMetaHelper.parseStringValue(domCustomProperty)
            ?: return

        val isAttributeTypeCatalogAware = TSMetaModelAccess.getInstance(project)
            .isCatalogAware(meta, qualifier, true)

        if (!isAttributeTypeCatalogAware) {
            holder.createProblem(
                domCustomProperty.value,
                severity,
                displayName
            )
        }
    }
}