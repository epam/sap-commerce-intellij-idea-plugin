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
package com.intellij.idea.plugin.hybris.system.bean.meta.impl

import com.intellij.idea.plugin.hybris.system.bean.BSUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModel
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelProcessor
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

class BSMetaModelProcessorImpl(private val myProject: Project) : BSMetaModelProcessor {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun process(psiFile: PsiFile): BSMetaModel? {
        psiFile.virtualFile ?: return null
        val module = BSUtils.getModuleForFile(psiFile) ?: return null
        val custom = BSUtils.isCustomExtensionFile(psiFile)
        val root = myDomManager.getFileElement(psiFile as XmlFile, Beans::class.java)
            ?.rootElement
            ?: return null

        return BSMetaModelBuilder(module, psiFile, custom)
            .withEnumTypes(root.enums)
            .withBeanTypes(root.beans)
            .withEventTypes(root.beans)
            .build()
    }

}