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

package com.intellij.idea.plugin.hybris.runConfigurations

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue

class LocalSapCXConfigurationType : SimpleConfigurationType(
    "SAPCX",
    "SAP CX Start",
    "Start local SAP CX Platform Configuration Type",
    NotNullLazyValue.createValue { HybrisIcons.Y.LOGO_BLUE }
) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration = LocalSapCXRunConfiguration(project, this)
}