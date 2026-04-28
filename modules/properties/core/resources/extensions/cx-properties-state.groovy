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
It returns a paged JSON object with `page`, `pageSize`, `totalItems`, and `items`.
*/

def configurationViewService = Registry.getApplicationContext()
        .getBean("configurationViewService", ConfigurationViewService)
def config = new HashMap<String, String>(configurationViewService.readConfigParameters())
def requestedPageToken = "[currentPagePlaceholder]"
def pageSizeToken = "[pageSizePlaceholder]"
def keyFilterToken = "[keyFilterPlaceholder]"
def valueFilterToken = "[valueFilterPlaceholder]"
def requestedPage = requestedPageToken.isInteger() ? requestedPageToken.toInteger() : 1
def pageSize = pageSizeToken.isInteger() ? pageSizeToken.toInteger() : 50
def keyFilter = keyFilterToken.startsWith("[") ? "" : keyFilterToken
def valueFilter = valueFilterToken.startsWith("[") ? "" : valueFilterToken

config.remove("extension.envs")
config.remove("extension.names")

def sortedEntries = config.entrySet()
        .findAll {
            (keyFilter.isEmpty() || it.key.toLowerCase().contains(keyFilter.toLowerCase())) &&
                    (valueFilter.isEmpty() || String.valueOf(it.value).toLowerCase().contains(valueFilter.toLowerCase()))
        }
        .sort { it.key }
def totalItems = sortedEntries.size()
def totalPages = Math.max(1, Math.ceil(totalItems / pageSize.toDouble()) as int)
def currentPage = Math.max(1, Math.min(requestedPage, totalPages))
def fromIndex = Math.min((currentPage - 1) * pageSize, totalItems)
def toIndex = Math.min(fromIndex + pageSize, totalItems)

return JsonOutput.toJson([
    page      : currentPage,
    pageSize  : pageSize,
    totalItems: totalItems,
    keyFilter : keyFilter,
    valueFilter: valueFilter,
    items     : sortedEntries
        .subList(fromIndex, toIndex)
        .collect { [key: it.key, value: it.value] }
])
