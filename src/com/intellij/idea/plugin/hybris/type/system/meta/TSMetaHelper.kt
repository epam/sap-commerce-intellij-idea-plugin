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

package com.intellij.idea.plugin.hybris.type.system.meta

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.type.system.model.AttributeModel
import com.intellij.idea.plugin.hybris.type.system.model.CustomProperties
import com.intellij.idea.plugin.hybris.type.system.model.CustomProperty

class TSMetaHelper {

    companion object {
        fun isDeprecated(dom: AttributeModel, name: String) = dom.setters
            .any { name == it.name.stringValue && java.lang.Boolean.TRUE == it.deprecated.value }

        fun isLocalized(type: String?) = type?.startsWith(HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX, true)
            ?: false

        fun isCatalogAware(dom: CustomProperties) = getProperty(dom, HybrisConstants.TS_CATALOG_ITEM_TYPE)
            ?.let { parseBooleanValue(it) }
            ?: false

        fun getProperty(dom: CustomProperties, name: String) = dom.properties
            .firstOrNull { name.equals(it.name.stringValue, true) }

        fun parseStringValue(customProperty: CustomProperty) = customProperty.value.rawText?.replace("\"", "")

        fun parseBooleanValue(customProperty: CustomProperty) =
            "java.lang.Boolean.TRUE" == customProperty.value.rawText
                    || "Boolean.TRUE" == customProperty.value.rawText

        fun parseIntValue(customProperty: CustomProperty) = customProperty.value.rawText
            ?.replace("Integer.valueOf(", "")
            ?.replace(")", "")
            ?.toIntOrNull()

        fun parseCommaSeparatedStringValue(customProperty: CustomProperty) = parseStringValue(customProperty)
            ?.split(",")
            ?.map { it.trim() }
    }
}