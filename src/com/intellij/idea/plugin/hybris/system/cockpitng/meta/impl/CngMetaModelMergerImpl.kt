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

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModel
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelMerger
import com.intellij.idea.plugin.hybris.system.type.meta.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.impl.*
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.openapi.project.Project
import java.util.*

class CngMetaModelMergerImpl(val myProject: Project) : CngMetaModelMerger {

    override fun merge(localMetaModels: Collection<CngMetaModel>) = with(CngGlobalMetaModel()) {
        localMetaModels
            .forEach { merge(this, it) }
        this
    }

    private fun merge(globalMetaModel: CngGlobalMetaModel, localMetaModel: CngMetaModel) {
        localMetaModel.getAllComponents()
            .forEach { globalMetaModel.addComponent(it) }
    }

}