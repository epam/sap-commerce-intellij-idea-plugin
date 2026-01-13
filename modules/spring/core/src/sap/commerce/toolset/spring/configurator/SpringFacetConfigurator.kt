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
package sap.commerce.toolset.spring.configurator

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.workspace.jps.entities.FacetEntity
import com.intellij.platform.workspace.jps.entities.FacetEntityTypeId
import com.intellij.platform.workspace.jps.entities.ModuleId
import com.intellij.spring.contexts.model.LocalXmlModel
import com.intellij.spring.facet.SpringFacet
import com.intellij.spring.facet.SpringFacetType
import com.intellij.util.io.URLUtil
import com.intellij.workspaceModel.ide.legacyBridge.findModule
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YBackofficeSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.project.facet.configurationXmlTag
import java.io.File

class SpringFacetConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Spring Facet"

    override fun isApplicable(moduleTypeId: String) = ProjectConstants.Y_MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(context: ProjectModuleConfigurationContext) {
        if (Plugin.SPRING.isDisabled()) return
        val javaModule = context.moduleEntity.findModule(context.workspaceModel.currentSnapshot) ?: return

        when (val moduleDescriptor = context.moduleDescriptor) {
            is YBackofficeSubModuleDescriptor -> return
            is PlatformModuleDescriptor -> configure(context, javaModule, emptySet())
            is YModuleDescriptor -> configure(context, javaModule, moduleDescriptor.getSpringFiles())
        }
    }

    private fun configure(
        context: ProjectModuleConfigurationContext,
        javaModule: Module,
        additionalFileSet: Set<String>
    ) {
        val moduleDescriptor = context.moduleDescriptor
        val moduleEntity = context.moduleEntity
        val facetType = SpringFacet.getSpringFacetType()
            .takeIf { it.isSuitableModuleType(ModuleType.get(javaModule)) }
            ?: return
        val facet = facetType.createFacet(
            javaModule,
            facetType.defaultFacetName,
            facetType.createDefaultConfiguration(), null
        )

        val facetName = moduleDescriptor.name + " - " + SpringFacet.FACET_TYPE_ID
        // dirty hack to trick IDEA autodetection of the web Spring context, see https://youtrack.jetbrains.com/issue/IDEA-257819
        // and WebXmlSpringWebModelContributor
        val facetId = if (moduleDescriptor is YWebSubModuleDescriptor) HybrisConstants.SPRING_WEB_FILE_SET_NAME
        else facetName

        val springFileSet = facet.addFileSet(facetId, facetName)
        springFileSet.isAutodetected = true

        additionalFileSet
            .mapNotNull { VfsUtil.findFileByIoFile(File(it), true) }
            .forEach { springFileSet.addFile(it) }
        additionalFileSet
            // jar://
            .filter { it.startsWith("${URLUtil.JAR_PROTOCOL}${URLUtil.SCHEME_SEPARATOR}") }
            .forEach { springFileSet.addFile(it) }

        facet.findSetting(LocalXmlModel.PROCESS_EXPLICITLY_ANNOTATED)
            ?.let {

                it.booleanValue = false
                it.apply()
            }

        val configurationXmlTag = facet.configurationXmlTag ?: return
        val facetEntityTypeId = FacetEntityTypeId(SpringFacetType.SPRING_FACET_TYPE__STRING_ID)
        val facetEntity = FacetEntity(
            moduleId = ModuleId(moduleEntity.name),
            name = facetType.presentableName,
            typeId = facetEntityTypeId,
            entitySource = moduleEntity.entitySource
        ) {
            this.configurationXmlTag = configurationXmlTag
        }

        context.importContext.mutableStorage.add(moduleEntity, facetEntity)
    }
}
