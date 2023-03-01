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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelMerger
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project

class CngMetaModelMergerImpl(val myProject: Project) : CngMetaModelMerger {

    override fun merge(
        configs: Collection<CngConfigMeta>,
        actions: Collection<CngMetaActionDefinition>,
        widgetDefinitions: Collection<CngMetaWidgetDefinition>,
        editors: Collection<CngMetaEditorDefinition>,
        widgets: Collection<CngMetaWidgets>
    ) = with(CngGlobalMetaModel()) {
        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.cng.access.progress.subTitle.merging")

        configs
            .forEach { merge(this, it) }
        actions
            .forEach { merge(this, it) }
        widgetDefinitions
            .forEach { merge(this, it) }
        editors
            .forEach { merge(this, it) }
        widgets
            .forEach { merge(this, it) }
        this
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMeta: CngConfigMeta) {
        localMeta.contexts
            .flatMap { it.attributes.entries }
            .forEach {
                globalMetaModel.contextAttributes.computeIfAbsent(it.key) { _ -> mutableSetOf() }
                globalMetaModel.contextAttributes[it.key]!!.add(it.value)
            }
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMeta: CngMetaActionDefinition) {
        globalMetaModel.actionDefinitions[localMeta.id] = localMeta
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMeta: CngMetaWidgetDefinition) {
        globalMetaModel.widgetDefinitions[localMeta.id] = localMeta
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMeta: CngMetaEditorDefinition) {
        globalMetaModel.editorDefinitions[localMeta.id] = localMeta
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMeta: CngMetaWidgets) {
        // It is possible to extend existing widget with new widgets, those have to be processed first
        localMeta.widgetExtensions
            .flatMap { it.widgets }
            .forEach { mergeWidget(it, globalMetaModel) }

        localMeta.widgets
            .forEach { mergeWidget(it, globalMetaModel) }
    }

    private fun mergeWidget(widget: CngMetaWidget, globalMetaModel: CngGlobalMetaModel) {
        globalMetaModel.widgets[widget.id] = widget

        widget.widgets.forEach { subWidget ->
            mergeWidget(subWidget, globalMetaModel)
        }
    }

}