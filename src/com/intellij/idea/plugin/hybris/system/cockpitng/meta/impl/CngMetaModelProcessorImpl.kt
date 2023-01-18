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
package com.intellij.idea.plugin.hybris.system.cockpitng.meta.impl

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngActionDefinitionMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngConfigMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelProcessor
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngWidgetDefinitionMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.ActionDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.WidgetDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

class CngMetaModelProcessorImpl(myProject: Project) : CngMetaModelProcessor {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun processConfig(psiFile: PsiFile): CngConfigMetaModel? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, Config::class.java)?.rootElement ?: return null

        return CngConfigMetaModel(psiFile, dom)
    }

    override fun processActionDefinition(psiFile: PsiFile): CngActionDefinitionMetaModel? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, ActionDefinition::class.java)?.rootElement ?: return null

        return CngActionDefinitionMetaModel(psiFile, dom)
    }

    override fun processWidgetDefinition(psiFile: PsiFile): CngWidgetDefinitionMetaModel? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, WidgetDefinition::class.java)?.rootElement ?: return null

        return CngWidgetDefinitionMetaModel(psiFile, dom)
    }

}