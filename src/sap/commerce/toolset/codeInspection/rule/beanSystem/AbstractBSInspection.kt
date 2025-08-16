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

package sap.commerce.toolset.codeInspection.rule.beanSystem

import sap.commerce.toolset.codeInspection.rule.AbstractInspection
import sap.commerce.toolset.system.bean.BSUtils
import sap.commerce.toolset.system.bean.model.Beans
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlFile

abstract class AbstractBSInspection : AbstractInspection<Beans>(Beans::class.java) {

    override fun canProcess(project: Project, file: XmlFile) = BSUtils.isBeansXmlFile(file)

    override fun canProcess(dom: Beans) = dom.xmlElement != null
}