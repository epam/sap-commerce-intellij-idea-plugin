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

package sap.commerce.toolset.project.configurator

import com.intellij.facet.FacetTypeRegistry
import com.intellij.openapi.util.JDOMUtil
import com.intellij.platform.workspace.jps.entities.FacetEntity
import com.intellij.platform.workspace.jps.entities.FacetEntityTypeId
import com.intellij.platform.workspace.jps.entities.ModuleId
import com.intellij.util.xmlb.XmlSerializer
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.facet.YFacetConstants
import sap.commerce.toolset.project.facet.YFacetType

class YFacetConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "SAP CX Facet"

    override fun isApplicable(moduleTypeId: String) = true

    override suspend fun configure(context: ProjectModuleConfigurationContext) {
        val moduleEntity = context.moduleEntity
        val facetType = FacetTypeRegistry.getInstance().findFacetType(YFacetConstants.Y_FACET_TYPE_ID)
        val xmlTag = XmlSerializer.serialize(context.moduleDescriptor.extensionDescriptor)
            .let { JDOMUtil.writeElement(it) }
        val facetEntityTypeId = FacetEntityTypeId(YFacetType.FACET_ID)

        val facetEntity = FacetEntity(
            moduleId = ModuleId(moduleEntity.name),
            name = facetType.presentableName,
            typeId = facetEntityTypeId,
            entitySource = moduleEntity.entitySource
        ) {
            this.configurationXmlTag = xmlTag
        }

        context.importContext.mutableStorage.add(moduleEntity, facetEntity)
    }
}
