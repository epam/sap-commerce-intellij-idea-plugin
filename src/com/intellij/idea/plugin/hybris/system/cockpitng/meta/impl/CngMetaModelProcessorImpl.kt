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

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelProcessor
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.*
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.*
import com.intellij.idea.plugin.hybris.system.type.meta.impl.CaseInsensitive.CaseInsensitiveConcurrentHashMap
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

class CngMetaModelProcessorImpl(myProject: Project) : CngMetaModelProcessor {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun processConfig(psiFile: PsiFile): CngConfigMeta? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, Config::class.java)?.rootElement ?: return null

        return CngConfigMeta(psiFile, dom)
    }

    override fun processActionDefinition(psiFile: PsiFile): CngMetaActionDefinition? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, ActionDefinition::class.java)?.rootElement ?: return null

        return CngMetaActionDefinition(psiFile, dom)
    }

    override fun processWidgetDefinition(psiFile: PsiFile): CngMetaWidgetDefinition? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, WidgetDefinition::class.java)?.rootElement ?: return null

        val settings = CaseInsensitiveConcurrentHashMap<String, CngMetaWidgetSetting>()
        dom.settings.settings
            .map { CngMetaWidgetSetting(psiFile, it) }
            .forEach { settings[it.id] = it }

        return CngMetaWidgetDefinition(psiFile, dom, settings)
    }

    override fun processEditorDefinition(psiFile: PsiFile): CngMetaEditorDefinition? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, EditorDefinition::class.java)?.rootElement ?: return null

        return CngMetaEditorDefinition(psiFile, dom)
    }

    override fun processWidgets(psiFile: PsiFile): CngMetaWidgets? {
        psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, Widgets::class.java)?.rootElement ?: return null

        return CngMetaWidgets(
            psiFile,
            dom,
            processWidgets(psiFile, dom.widgets),
            dom.widgetExtensions
                .map { CngMetaWidgetExtension(psiFile, it, processWidgets(psiFile, it.widgets)) }
        )
    }

    private fun processWidgets(
        psiFile: PsiFile,
        widgets: List<Widget>
    ): List<CngMetaWidget> = widgets
        // if ID is null we may need to re-index the project, faced such issue due broken Stubs
        .filter { it.id.exists() }
        .map {
            val subWidgets = if (it.widgets.isNotEmpty()) processWidgets(psiFile, it.widgets)
            else emptyList()
            CngMetaWidget(psiFile, it, subWidgets)
        }

}