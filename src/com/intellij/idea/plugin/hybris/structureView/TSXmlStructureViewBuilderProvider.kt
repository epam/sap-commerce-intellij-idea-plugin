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
package com.intellij.idea.plugin.hybris.structureView

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider
import com.intellij.idea.plugin.hybris.system.type.model.Attributes
import com.intellij.idea.plugin.hybris.system.type.model.CustomProperties
import com.intellij.idea.plugin.hybris.system.type.model.Indexes
import com.intellij.idea.plugin.hybris.system.type.utils.TSUtils
import com.intellij.psi.xml.XmlFile
import com.intellij.util.Function
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomService

class TSXmlStructureViewBuilderProvider : XmlStructureViewBuilderProvider {

    override fun createStructureViewBuilder(xmlFile: XmlFile): StructureViewBuilder? {
        if (!TSUtils.isTypeSystemFile(xmlFile)) return null

        return TSStructureViewBuilder(xmlFile, descriptor)
    }

    companion object {
        private val descriptor = Function { dom: DomElement ->
            return@Function when (dom) {
                is Attributes -> DomService.StructureViewMode.SHOW_CHILDREN
                is Indexes -> DomService.StructureViewMode.SHOW_CHILDREN
                is CustomProperties -> DomService.StructureViewMode.SHOW_CHILDREN
                else -> DomService.StructureViewMode.SHOW
            }
        }
    }
}