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
package sap.commerce.toolset.kotlin.configurator

import com.intellij.facet.FacetTypeRegistry
import com.intellij.openapi.util.JDOMUtil
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.FacetEntity
import com.intellij.platform.workspace.jps.entities.FacetEntityTypeId
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.ModuleId
import com.intellij.util.xmlb.XmlSerializer
import org.jetbrains.kotlin.idea.facet.KotlinFacetType
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.util.directoryExists

class KotlinFacetConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Kotlin Facet"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        if (moduleDescriptor !is YModuleDescriptor) return

        importContext.chosenHybrisModuleDescriptors
            .firstOrNull { EiConstants.Extension.KOTLIN_NATURE == it.name }
            ?: return

        val hasKotlinDirectories = hasKotlinDirectories(moduleDescriptor)

        if (!hasKotlinDirectories) return

        val facetType = FacetTypeRegistry.getInstance().findFacetType(KotlinFacetType.TYPE_ID)
        val xmlTag = XmlSerializer.serialize(KotlinFacetType.INSTANCE.createDefaultConfiguration())
            .let { JDOMUtil.writeElement(it) }
        val facetEntityTypeId = FacetEntityTypeId(KotlinFacetType.ID)

        val facetEntity = FacetEntity(
            moduleId = ModuleId(moduleEntity.name),
            name = facetType.presentableName,
            typeId = facetEntityTypeId,
            entitySource = moduleEntity.entitySource
        ) {
            this.configurationXmlTag = xmlTag
        }

        importContext.mutableStorage.add(moduleEntity, facetEntity)
    }

    private fun hasKotlinDirectories(descriptor: ModuleDescriptor) = descriptor.moduleRootPath.resolve(ProjectConstants.Directory.KOTLIN_SRC).directoryExists
        || descriptor.moduleRootPath.resolve(ProjectConstants.Directory.KOTLIN_TEST_SRC).directoryExists

}
