/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.type.system.meta.impl

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModel
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelProcessor
import com.intellij.idea.plugin.hybris.type.system.meta.model.impl.TSMetaModelBuilder
import com.intellij.idea.plugin.hybris.type.system.model.Items
import com.intellij.idea.plugin.hybris.type.system.model.TypeGroup
import com.intellij.idea.plugin.hybris.type.system.utils.TypeSystemUtils
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import java.util.stream.Collectors

class TSMetaModelProcessorImpl(private val myProject: Project) : TSMetaModelProcessor {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun process(psiFile: PsiFile): TSMetaModel? {
        psiFile.virtualFile ?: return null
        val module = TypeSystemUtils.getModuleForFile(psiFile) ?: return null
        val custom = TypeSystemUtils.isCustomExtensionFile(psiFile)
        val rootWrapper = myDomManager.getFileElement(psiFile as XmlFile, Items::class.java)

        rootWrapper ?: return null

        val items = rootWrapper.rootElement

        return TSMetaModelBuilder(module, psiFile, custom)
            .withItemTypes(items.itemTypes.itemTypes)
            .withItemTypes(items.itemTypes.typeGroups.stream()
                .flatMap { tg: TypeGroup -> tg.itemTypes.stream() }
                .collect(Collectors.toList()))
            .withEnumTypes(items.enumTypes.enumTypes)
            .withAtomicTypes(items.atomicTypes.atomicTypes)
            .withCollectionTypes(items.collectionTypes.collectionTypes)
            .withRelationTypes(items.relations.relations)
            .withMapTypes(items.mapTypes.mapTypes)
            .build()
    }

}