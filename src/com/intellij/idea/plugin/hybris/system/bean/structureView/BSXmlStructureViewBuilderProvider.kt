/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.bean.structureView

import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider
import com.intellij.idea.plugin.hybris.system.bean.BSUtils
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomService

class BSXmlStructureViewBuilderProvider : XmlStructureViewBuilderProvider {

    override fun createStructureViewBuilder(xmlFile: XmlFile) = if (!BSUtils.isBeansXmlFile(xmlFile)) null
    else  BSStructureViewBuilder(xmlFile) { _: DomElement -> DomService.StructureViewMode.SHOW }

}