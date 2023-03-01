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
package com.intellij.idea.plugin.hybris.system.type.meta.impl

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModel
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelProcessor
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.TypeGroup
import com.intellij.idea.plugin.hybris.system.type.utils.TSUtils
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import java.util.stream.Collectors

class TSMetaModelProcessorImpl(private val myProject: Project) : TSMetaModelProcessor {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun process(psiFile: PsiFile): TSMetaModel? {
        psiFile.virtualFile ?: return null
        val module = TSUtils.getModuleForFile(psiFile) ?: return null
        val custom = TSUtils.isCustomExtensionFile(psiFile)
        val rootWrapper = myDomManager.getFileElement(psiFile as XmlFile, Items::class.java)

        rootWrapper ?: return null

        val items = rootWrapper.rootElement

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.ts.access.progress.subTitle.processing", psiFile.name)

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