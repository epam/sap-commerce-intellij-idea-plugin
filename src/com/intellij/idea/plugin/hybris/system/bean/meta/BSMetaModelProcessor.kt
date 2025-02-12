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
package com.intellij.idea.plugin.hybris.system.bean.meta

import com.intellij.idea.plugin.hybris.system.bean.BSUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.impl.BSMetaModelBuilder
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import kotlinx.coroutines.*

@Service(Service.Level.PROJECT)
class BSMetaModelProcessor(myProject: Project) {

    companion object {
        fun getInstance(project: Project): BSMetaModelProcessor = project.getService(BSMetaModelProcessor::class.java)
    }

    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    suspend fun process(coroutineScope: CoroutineScope, psiFile: PsiFile): BSMetaModel? = coroutineScope {
        psiFile.virtualFile ?: return@coroutineScope null
        val module = BSUtils.getModuleForFile(psiFile)
            ?: return@coroutineScope null
        val custom = BSUtils.isCustomExtensionFile(psiFile)
        val root = myDomManager.getFileElement(psiFile as XmlFile, Beans::class.java)
            ?.rootElement
            ?: return@coroutineScope null

        val builder = BSMetaModelBuilder(module, psiFile, custom)

        val operations = listOf(
            coroutineScope.async { builder.withEnumTypes(root.enums) },
            coroutineScope.async { builder.withBeanTypes(root.beans) },
            coroutineScope.async { builder.withEventTypes(root.beans) },
        )

        withContext(Dispatchers.IO) {
            operations.awaitAll()
        }

        builder.build()
    }
}