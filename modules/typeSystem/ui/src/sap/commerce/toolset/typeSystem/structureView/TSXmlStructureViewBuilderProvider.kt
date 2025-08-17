/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.typeSystem.structureView

import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomService
import sap.commerce.toolset.typeSystem.model.Attributes
import sap.commerce.toolset.typeSystem.model.CustomProperties
import sap.commerce.toolset.typeSystem.model.Indexes
import sap.commerce.toolset.typeSystem.util.TSUtils

class TSXmlStructureViewBuilderProvider : XmlStructureViewBuilderProvider {

    override fun createStructureViewBuilder(xmlFile: XmlFile) = if (!TSUtils.isTypeSystemFile(xmlFile)) null
    else TSStructureViewBuilder(xmlFile) { dom: DomElement ->
        when (dom) {
            is Attributes -> DomService.StructureViewMode.SHOW_CHILDREN
            is Indexes -> DomService.StructureViewMode.SHOW_CHILDREN
            is CustomProperties -> DomService.StructureViewMode.SHOW_CHILDREN
            else -> DomService.StructureViewMode.SHOW
        }
    }

}