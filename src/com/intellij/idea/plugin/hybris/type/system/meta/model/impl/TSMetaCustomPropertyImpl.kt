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
package com.intellij.idea.plugin.hybris.type.system.meta.model.impl

import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty
import com.intellij.idea.plugin.hybris.type.system.model.CustomProperty
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

class TSMetaCustomPropertyImpl(
    override val module: Module,
    override val project: Project,
    dom: CustomProperty,
    override val isCustom: Boolean,
    override val name: String
) : TSMetaEntityImpl<CustomProperty>(dom, module, project, isCustom, name),
    TSMetaCustomProperty {

    override val value = myDom.value.rawText

    override fun toString(): String {
        return "TSMetaCustomPropertyImpl(module=$module, name=$name, isCustom=$isCustom)"
    }
}