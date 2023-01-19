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

package com.intellij.idea.plugin.hybris.system.cockpitng.util.xml

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelAccess
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.ActionDefinition
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.ResolvingConverter

class CngActionDefinitionIdConverter : ResolvingConverter<ActionDefinition>() {

    override fun toString(t: ActionDefinition?, context: ConvertContext?) = t?.id?.stringValue

    override fun fromString(s: String?, context: ConvertContext): ActionDefinition? {
        if (s == null) return null

        return CngMetaModelAccess.getInstance(context.project).getMetaModel()
            .actionDefinitions[s]
            ?.retrieveDom()
    }

    override fun getVariants(context: ConvertContext): Collection<ActionDefinition> {
        return CngMetaModelAccess.getInstance(context.project).getMetaModel()
            .actionDefinitions
            .values
            .mapNotNull { it.retrieveDom() }
    }

}