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

package com.intellij.idea.plugin.hybris.runConfigurations

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.SystemUtils


class LocalSapCXRunConfigurationOptions() : RunConfigurationOptions() {


    fun getScriptName(): String {

        val hybrisServerScript: String = if (SystemUtils.IS_OS_WINDOWS) HybrisConstants.HYBRIS_SERVER_BASH_SCRIPT_NAME else HybrisConstants.HYBRIS_SERVER_SHELL_SCRIPT_NAME

        return hybrisServerScript
    }
}
