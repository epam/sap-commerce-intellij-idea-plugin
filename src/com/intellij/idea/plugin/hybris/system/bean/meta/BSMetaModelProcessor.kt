/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.system.bean.BSUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.impl.BSMetaModelBuilder
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

@Service(Service.Level.PROJECT)
class BSMetaModelProcessor(myProject: Project) {

    companion object {
        fun getInstance(project: Project): BSMetaModelProcessor = project.getService(BSMetaModelProcessor::class.java)
    }
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    fun process(psiFile: PsiFile): BSMetaModel? {
        psiFile.virtualFile ?: return null
        val module = BSUtils.getModuleForFile(psiFile) ?: return null
        val custom = BSUtils.isCustomExtensionFile(psiFile)
        val root = myDomManager.getFileElement(psiFile as XmlFile, Beans::class.java)
            ?.rootElement
            ?: return null

        ProgressManager.getInstance().progressIndicator.text2 = HybrisI18NBundleUtils.message("hybris.bs.access.progress.subTitle.processing", psiFile.name)

        return BSMetaModelBuilder(module, psiFile, custom)
            .withEnumTypes(root.enums)
            .withBeanTypes(root.beans)
            .withEventTypes(root.beans)
            .build()
    }
}