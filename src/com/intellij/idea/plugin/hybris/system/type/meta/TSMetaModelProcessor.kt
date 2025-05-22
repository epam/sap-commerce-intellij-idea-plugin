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
package com.intellij.idea.plugin.hybris.system.type.meta

import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.meta.FoundMeta
import com.intellij.idea.plugin.hybris.system.type.meta.impl.TSMetaModelBuilder
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.coroutineScope
import kotlin.time.measureTimedValue

@Service(Service.Level.PROJECT)
class TSMetaModelProcessor(private val project: Project) {

    suspend fun process(foundMeta: FoundMeta<Items>): TSMetaModel? = coroutineScope {
        readAction {
            val (v, d) = measureTimedValue {
                val moduleName = foundMeta.moduleName
                val extensionName = foundMeta.extensionName
                val items = foundMeta.rootElement
                val fileName = foundMeta.name
                val custom = PsiUtils.isCustomExtensionFile(foundMeta.virtualFile, project)

                with(TSMetaModelBuilder(moduleName, extensionName, fileName, custom)) {
                    withItemTypes(items.itemTypes.itemTypes)
                    withItemTypes(items.itemTypes.typeGroups.flatMap { it.itemTypes })
                    withEnumTypes(items.enumTypes.enumTypes)
                    withAtomicTypes(items.atomicTypes.atomicTypes)
                    withCollectionTypes(items.collectionTypes.collectionTypes)
                    withRelationTypes(items.relations.relations)
                    withMapTypes(items.mapTypes.mapTypes)
                    build()
                }
            }
            println("ts new process - ${foundMeta.name} - ${d.inWholeMilliseconds}")

            return@readAction v
        }
    }
}