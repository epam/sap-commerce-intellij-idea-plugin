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
package com.intellij.idea.plugin.hybris.system.type.meta.model.impl

import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaDeployment
import com.intellij.idea.plugin.hybris.system.type.model.Deployment
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaDeploymentImpl(
    dom: Deployment,
    override val moduleName: String,
    override val extensionName: String,
    override val name: String?,
    override var isCustom: Boolean
) : TSMetaDeployment {

    override val domAnchor: DomAnchor<Deployment> = DomService.getInstance().createAnchor(dom)
    override val propertyTable = dom.propertyTable.stringValue ?: "props"
    override val typeCode = dom.typeCode.stringValue
    override val table = dom.table.stringValue

    override fun toString() = "Deployment(module=$extensionName, name=$name, isCustom=$isCustom)"
}
