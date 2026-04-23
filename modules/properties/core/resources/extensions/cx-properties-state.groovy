/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import de.hybris.platform.core.Registry
import de.hybris.platform.servicelayer.config.ConfigurationViewService
import groovy.json.JsonOutput

/*
======= Version: 2026.4.23 =======

This script is used by the plugin to retrieve SAP Commerce runtime properties.
It returns a JSON array of objects with `key` and `value` fields.
*/

def configurationViewService = Registry.getApplicationContext()
        .getBean("configurationViewService", ConfigurationViewService)
def config = new HashMap<String, String>(configurationViewService.readConfigParameters())

config.remove("extension.envs")
config.remove("extension.names")

return JsonOutput.toJson(
    config.entrySet()
        .sort { it.key }
        .collect { [key: it.key, value: it.value] }
)
