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

package com.intellij.idea.plugin.hybris.system.cockpitng

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.ActionDefinition
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomFileDescription
import javax.swing.Icon

class CngActionDefinitionDomFileDescription : DomFileDescription<ActionDefinition>(ActionDefinition::class.java, "action-definition") {

    override fun getFileIcon(flags: Int): Icon = HybrisIcons.COCKPIT_NG_ACTION_DEFINITION

    override fun isMyFile(file: XmlFile, module: Module?) = super.isMyFile(file, module)
        && (module != null || ModuleUtil.projectContainsFile(file.project, file.virtualFile, true))
        && file.name == HybrisConstants.COCKPIT_NG_DEFINITION_XML
        && HybrisProjectSettingsComponent.getInstance(file.project).isHybrisProject()
}