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

package sap.commerce.toolset.jrebel.configurator

import com.intellij.facet.FacetType
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.util.io.NioFiles
import com.zeroturnaround.javarebel.idea.plugin.actions.ToggleRebelFacetAction
import com.zeroturnaround.javarebel.idea.plugin.facet.JRebelFacet
import com.zeroturnaround.javarebel.idea.plugin.facet.JRebelFacetType
import com.zeroturnaround.javarebel.idea.plugin.xml.RebelXML
import org.zeroturnaround.jrebel.client.config.JRebelConfiguration
import sap.commerce.toolset.project.configurator.ProjectPostImportAsyncConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext
import sap.commerce.toolset.project.descriptor.YSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCustomRegularModuleDescriptor
import java.io.File

class JRebelConfigurator : ProjectPostImportAsyncConfigurator {

    override val name: String
        get() = "JRebel"

    override suspend fun configure(context: ProjectPostImportContext) {
        val writeOperations = context.chosenHybrisModuleDescriptors
            .filter { it is YCustomRegularModuleDescriptor || (it is YSubModuleDescriptor && it.owner is YCustomRegularModuleDescriptor) }
            .mapNotNull { moduleDescriptor ->
                context.modules[moduleDescriptor.name] ?: run {
                    thisLogger().warn("Could not find module for ${moduleDescriptor.name}")
                    return@mapNotNull null
                }
            }
            .mapNotNull { module ->
                readAction { JRebelFacet.getInstance(module) } ?: return@mapNotNull null
                readAction {
                    FacetType.findInstance(JRebelFacetType::class.java)
                        .takeUnless { it.isSuitableModuleType(ModuleType.get(module)) }
                } ?: return@mapNotNull null

                // To ensure regeneration of the rebel.xml,
                // we may need to remove backup created by the JRebel plugin on module removal during the Project Refresh.
                val xml = readAction { RebelXML.getInstance(module) }
                val backupHash = xml.backupHash()

                val backupDirectory = readAction {
                    File(JRebelConfiguration.getUserHomeDir(), "xml-backups/$backupHash")
                        .takeIf { it.exists() && it.isDirectory }
                        ?.toPath()
                }

                val writeOperation = {
                    backupDirectory?.let { NioFiles.deleteRecursively(it) }

                    ToggleRebelFacetAction.conditionalEnableJRebelFacet(module, false, false)
                }

                writeOperation
            }


        backgroundWriteAction {
            writeOperations.forEach { it() }
        }
    }

}