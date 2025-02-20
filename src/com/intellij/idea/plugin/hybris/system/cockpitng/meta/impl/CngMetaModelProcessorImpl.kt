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
package com.intellij.idea.plugin.hybris.system.cockpitng.meta.impl

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelProcessor
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.*
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.*
import com.intellij.idea.plugin.hybris.system.type.meta.impl.CaseInsensitive.CaseInsensitiveConcurrentHashMap
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

class CngMetaModelProcessorImpl(myProject: Project) : CngMetaModelProcessor {
    private val myDomManager: DomManager = DomManager.getDomManager(myProject)

    override fun processConfig(psiFile: PsiFile): CngConfigMeta? {
        val virtualFile = psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, Config::class.java)?.rootElement ?: return null

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.cng.access.progress.subTitle.processing", virtualFile.name)

        val contexts = dom.contexts
            .filter { it.component.stringValue != null }
            .filter { it.component.stringValue!!.isNotBlank() }
            .map { CngContextMeta(virtualFile, it, it.component.stringValue!!) }
        return CngConfigMeta(virtualFile, dom, contexts)
    }

    override fun processActionDefinition(psiFile: PsiFile): CngMetaActionDefinition? {
        val virtualFile = psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, ActionDefinition::class.java)
            ?.rootElement
            ?: return null
        val id = CngMetaModelNameProvider.extract(dom) ?: return null

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.cng.access.progress.subTitle.processing", virtualFile.name)

        return CngMetaActionDefinition(virtualFile, dom, id)
    }

    override fun processWidgetDefinition(psiFile: PsiFile): CngMetaWidgetDefinition? {
        val virtualFile = psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, WidgetDefinition::class.java)
            ?.rootElement
            ?: return null
        val id = CngMetaModelNameProvider.extract(dom) ?: return null

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.cng.access.progress.subTitle.processing", virtualFile.name)

        val settings = CaseInsensitiveConcurrentHashMap<String, CngMetaWidgetSetting>()
        dom.settings.settings
            .map { CngMetaWidgetSetting(virtualFile, it) }
            .forEach { settings[it.id] = it }

        return CngMetaWidgetDefinition(virtualFile, dom, id, settings)
    }

    override fun processEditorDefinition(psiFile: PsiFile): CngMetaEditorDefinition? {
        val virtualFile = psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, EditorDefinition::class.java)
            ?.rootElement
            ?: return null
        val id = CngMetaModelNameProvider.extract(dom) ?: return null

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.cng.access.progress.subTitle.processing", virtualFile.name)

        return CngMetaEditorDefinition(virtualFile, dom, id)
    }

    override fun processWidgets(psiFile: PsiFile): CngMetaWidgets? {
        val virtualFile = psiFile.virtualFile ?: return null
        val dom = myDomManager.getFileElement(psiFile as XmlFile, Widgets::class.java)
            ?.rootElement
            ?: return null

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.cng.access.progress.subTitle.processing", virtualFile.name)

        return CngMetaWidgets(
            virtualFile,
            dom,
            processWidgets(virtualFile, dom.widgets),
            dom.widgetExtensions
                .filter { it.widgetId.stringValue?.isNotBlank() ?: false }
                .map { CngMetaWidgetExtension(virtualFile, it, processWidgets(virtualFile, it.widgets)) }
        )
    }

    private fun processWidgets(
        virtualFile: VirtualFile,
        widgets: List<Widget>
    ): List<CngMetaWidget> = widgets
        // if ID is null we may need to re-index the project, faced such issue due broken Stubs
        .filter { it.id.exists() }
        .filter { it.id.stringValue?.isNotBlank() ?: false }
        .map {
            val subWidgets = if (it.widgets.isNotEmpty()) processWidgets(virtualFile, it.widgets)
            else emptyList()
            CngMetaWidget(virtualFile, it, subWidgets)
        }

}