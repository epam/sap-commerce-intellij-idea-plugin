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

import com.intellij.idea.plugin.hybris.system.type.meta.impl.TSMetaModelBuilder
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.util.TSUtils
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import kotlinx.coroutines.*

@Service(Service.Level.PROJECT)
class TSMetaModelProcessor(myProject: Project) {

    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    suspend fun process(coroutineScope: CoroutineScope, psiFile: PsiFile): TSMetaModel? = coroutineScope {
        psiFile.virtualFile ?: return@coroutineScope null
        val module = TSUtils.getModuleForFile(psiFile)
            ?: return@coroutineScope null
        val custom = TSUtils.isCustomExtensionFile(psiFile)
        val rootWrapper = myDomManager.getFileElement(psiFile as XmlFile, Items::class.java)

        rootWrapper ?: return@coroutineScope null

        val items = rootWrapper.rootElement

        val builder = TSMetaModelBuilder(module, psiFile, custom)

        val operations = listOf(
            coroutineScope.async { builder.withItemTypes(items.itemTypes.itemTypes) },
            coroutineScope.async { builder.withItemTypes(items.itemTypes.typeGroups.flatMap { it.itemTypes }) },
            coroutineScope.async { builder.withEnumTypes(items.enumTypes.enumTypes) },
            coroutineScope.async { builder.withAtomicTypes(items.atomicTypes.atomicTypes) },
            coroutineScope.async { builder.withCollectionTypes(items.collectionTypes.collectionTypes) },
            coroutineScope.async { builder.withRelationTypes(items.relations.relations) },
            coroutineScope.async { builder.withMapTypes(items.mapTypes.mapTypes) },
        )

        withContext(Dispatchers.IO) {
            operations.awaitAll()
        }

        builder.build()
    }

    companion object {
        fun getInstance(project: Project): TSMetaModelProcessor = project.getService(TSMetaModelProcessor::class.java)
    }
}