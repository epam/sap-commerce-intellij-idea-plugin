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

package com.intellij.idea.plugin.hybris.type.system.meta.model

import com.intellij.idea.plugin.hybris.type.system.model.CustomProperty

interface TSMetaCustomProperty : TSMetaClassifier<CustomProperty> {
    override val name: String
    val rawValue: String?

    object KnownProperties {
        val UNIQUE_KEY_ATTRIBUTE_QUALIFIER = "uniqueKeyAttributeQualifier"
        val CATALOG_ITEM_TYPE = "catalogItemType"
        val CATALOG_VERSION_ATTRIBUTE_QUALIFIER = "catalogVersionAttributeQualifier"
        val CATALOG_SYNC_DEFAULT_ROOT_TYPE = "catalog.sync.default.root.type"
        val CATALOG_SYNC_DEFAULT_ROOT_TYPE_ORDER = "catalog.sync.default.root.type.order"
    }
}
