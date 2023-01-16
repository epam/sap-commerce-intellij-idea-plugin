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

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelProcessor
import com.intellij.idea.plugin.hybris.system.cockpitng.model.Config
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

class CngMetaModelProcessorImpl(private val myProject: Project) : CngMetaModelProcessor {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun process(psiFile: PsiFile): CngMetaModel? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, Config::class.java)?.rootElement ?: return null

        return with(CngMetaModel(psiFile)) {
            dom.contexts
                .mapNotNull { it.component.stringValue }
                .toSet()
                .let { it.forEach { component -> this.addComponent(component) } }

            this
        }
    }

}