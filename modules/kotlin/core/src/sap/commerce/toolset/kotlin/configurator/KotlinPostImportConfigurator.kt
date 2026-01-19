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

import com.intellij.facet.FacetManager
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.entities
import com.intellij.workspaceModel.ide.legacyBridge.findModule
import org.jetbrains.kotlin.idea.facet.KotlinFacetType
import sap.commerce.toolset.project.configurator.ProjectPostImportAsyncConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext

class KotlinPostImportConfigurator : ProjectPostImportAsyncConfigurator {

    override val name: String
        get() = "Kotlin"

    override suspend fun configure(context: ProjectPostImportContext) {
        val hasKotlinNatureExtension = context.hasKotlinNatureExtension
        if (hasKotlinNatureExtension) return

        removeKotlinFacets(context)
    }

    private suspend fun removeKotlinFacets(context: ProjectPostImportContext) {
        val storage = context.storage
        val writeActions = readAction {
            storage.entities<ModuleEntity>()
                .mapNotNull { it.findModule(storage) }
                .mapNotNull { module ->
                    val facetManager = FacetManager.getInstance(module)
                    val kotlinFacet = facetManager.getFacetByType(KotlinFacetType.TYPE_ID)
                        ?: return@mapNotNull null
                    val createModifiableModel = facetManager.createModifiableModel()

                    val writeAction = {
                        createModifiableModel.removeFacet(kotlinFacet)
                        createModifiableModel.commit()
                    }
                    writeAction
                }
        }

        backgroundWriteAction {
            writeActions.forEach { it() }
        }
    }
}
