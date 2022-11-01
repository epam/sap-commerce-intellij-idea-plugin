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
import com.intellij.idea.plugin.hybris.type.system.meta.model.MetaType
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation
import com.intellij.idea.plugin.hybris.type.system.model.AttributeModel
import com.intellij.idea.plugin.hybris.type.system.model.CustomProperties
import com.intellij.idea.plugin.hybris.type.system.model.CustomProperty
import java.util.*

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

        fun getAllExtends(metaModel: TSGlobalMetaModel, meta: TSGlobalMetaItem): Set<TSGlobalMetaItem> {
            val tempParents = LinkedHashSet<TSGlobalMetaItem>()
            var metaItem = getMetaItem(metaModel, meta)

            while (metaItem != null) {
                tempParents.add(metaItem)
                metaItem = getMetaItem(metaModel, metaItem)
            }
            return Collections.unmodifiableSet(tempParents)
        }

        fun getAllRelationEnds(metaModel: TSGlobalMetaModel, meta: TSGlobalMetaItem, extends: Set<TSGlobalMetaItem>): Collection<TSMetaRelation.TSMetaRelationElement> {
            val currentMetaRelationEnds = getMetaRelationEnds(metaModel, meta)
            val extendsMetaRelationEnds = extends.flatMap { metaExtend -> getMetaRelationEnds(metaModel, metaExtend)}
            return currentMetaRelationEnds + extendsMetaRelationEnds
        }

        private fun getMetaItem(metaModel: TSGlobalMetaModel, meta: TSGlobalMetaItem): TSGlobalMetaItem? {
            val realExtendedMetaItemName = meta.extendedMetaItemName ?: HybrisConstants.TS_TYPE_GENERIC_ITEM

            return metaModel.getMetaType<TSGlobalMetaItem>(MetaType.META_ITEM)[realExtendedMetaItemName]
        }

        private fun getMetaRelationEnds(metaModel: TSGlobalMetaModel, meta: TSGlobalMetaItem): Collection<TSMetaRelation.TSMetaRelationElement> {
            val name = meta.name ?: return emptyList()
            return metaModel.getRelations(name) ?: emptyList()
        }
    }
}