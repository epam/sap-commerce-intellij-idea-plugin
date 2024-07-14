/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.type.file

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.Iconable.IconFlags
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomFileDescription
import javax.swing.Icon

class TSDomFileDescription : DomFileDescription<Items>(Items::class.java, HybrisConstants.ROOT_TAG_ITEMS_XML) {

    override fun getFileIcon(@IconFlags flags: Int): Icon = HybrisIcons.TYPE_SYSTEM

    override fun isMyFile(
        file: XmlFile, module: Module?
    ) = super.isMyFile(file, module)
        && file.name.endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING)
        && hasNamespace(file)
        && ProjectSettingsComponent.getInstance(file.project).isHybrisProject()
        && (module != null || ModuleUtil.projectContainsFile(file.project, file.viewProvider.virtualFile, false))

    private fun hasNamespace(file: XmlFile) = file.rootTag
            ?.attributes
            ?.any { it.localName == "noNamespaceSchemaLocation" && it.value == "items.xsd" }
            ?: false
}
