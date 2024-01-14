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

import com.intellij.execution.configurations.ModuleBasedConfigurationOptions
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.OptionTag

class LocalSapCXRunnerOptions : ModuleBasedConfigurationOptions() {


    @get:OptionTag(tag = "remoteDebugPort", valueAttribute = "remoteDebugPort", nameAttribute = "")
    var remoteDebugPort: String? by string(HybrisConstants.DEBUG_PORT)

    @get:OptionTag(tag = "remoteDebugHost", valueAttribute = "remoteDebugHost", nameAttribute = "")
    var remoteDebugHost: String? by string(HybrisConstants.DEBUG_HOST)

    @get:OptionTag
    @get:MapAnnotation(sortBeforeSave = false)
    var environmentProperties: MutableMap<String, String> by linkedMap()

    @get:OptionTag(tag = "isPasParentEnv", valueAttribute = "isPasParentEnv", nameAttribute = "")
    var isPassParentEnv: Boolean by property(true)

}