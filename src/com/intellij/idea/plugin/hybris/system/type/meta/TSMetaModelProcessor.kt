/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.openapi.application.readActionBlocking
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

@Service(Service.Level.PROJECT)
class TSMetaModelProcessor(myProject: Project) {

    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    suspend fun process(coroutineScope: CoroutineScope, psiFile: PsiFile): TSMetaModel? = readActionBlocking {
        psiFile.virtualFile ?: return@readActionBlocking null
        val module = TSUtils.getModuleForFile(psiFile) ?: return@readActionBlocking null
        val custom = TSUtils.isCustomExtensionFile(psiFile)
        val rootWrapper = myDomManager.getFileElement(psiFile as XmlFile, Items::class.java)

        rootWrapper ?: return@readActionBlocking null

        val items = rootWrapper.rootElement

        val builder = TSMetaModelBuilder(module, psiFile, custom)

        val operations = listOf(
            coroutineScope.async { readActionBlocking { builder.withItemTypes(items.itemTypes.itemTypes) } },
            coroutineScope.async { readActionBlocking { builder.withItemTypes(items.itemTypes.typeGroups.flatMap { it.itemTypes }) } },
            coroutineScope.async { readActionBlocking { builder.withEnumTypes(items.enumTypes.enumTypes) } },
            coroutineScope.async { readActionBlocking { builder.withAtomicTypes(items.atomicTypes.atomicTypes) } },
            coroutineScope.async { readActionBlocking { builder.withCollectionTypes(items.collectionTypes.collectionTypes) } },
            coroutineScope.async { readActionBlocking { builder.withRelationTypes(items.relations.relations) } },
            coroutineScope.async { readActionBlocking { builder.withMapTypes(items.mapTypes.mapTypes) } },
        )

        runBlocking(Dispatchers.IO) {
            operations.awaitAll()
        }

        builder.build()
    }

    companion object {
        fun getInstance(project: Project): TSMetaModelProcessor = project.getService(TSMetaModelProcessor::class.java)
    }
}