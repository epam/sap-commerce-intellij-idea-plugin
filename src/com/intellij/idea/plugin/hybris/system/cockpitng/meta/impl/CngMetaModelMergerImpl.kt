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

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.*
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngActionDefinitionMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngConfigMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.CngWidgetDefinitionMetaModel
import com.intellij.openapi.project.Project

class CngMetaModelMergerImpl(val myProject: Project) : CngMetaModelMerger {

    override fun merge(
        configs: Collection<CngConfigMetaModel>,
        actions: Collection<CngActionDefinitionMetaModel>,
        widgets: Collection<CngWidgetDefinitionMetaModel>
    ) = with(CngGlobalMetaModel()) {
        configs
            .forEach { merge(this, it) }
        actions
            .forEach { merge(this, it) }
        widgets
            .forEach { merge(this, it) }
        this
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMetaModel: CngConfigMetaModel) {
        globalMetaModel.components.addAll(localMetaModel.getAllComponents())
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMetaModel: CngActionDefinitionMetaModel) {
        globalMetaModel.actionDefinitions.put(localMetaModel.id, localMetaModel)
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMetaModel: CngWidgetDefinitionMetaModel) {
        globalMetaModel.widgetDefinitions.put(localMetaModel.id, localMetaModel)
    }

}