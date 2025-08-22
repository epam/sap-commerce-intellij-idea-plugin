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

package sap.commerce.toolset.businessProcess

import com.intellij.openapi.module.Module
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomFileDescription
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.businessProcess.model.Process
import sap.commerce.toolset.isHybrisProject
import javax.swing.Icon

class BpDomFileDescription : DomFileDescription<Process>(Process::class.java, HybrisConstants.ROOT_TAG_BUSINESS_PROCESS_XML) {

    override fun getFileIcon(flags: Int): Icon = HybrisIcons.BusinessProcess.FILE

    override fun isMyFile(file: XmlFile, module: Module?) = super.isMyFile(file, module)
        && file.isHybrisProject
}