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

package com.intellij.idea.plugin.hybris.system.type.meta

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.*
import java.util.*

class TSMetaHelper {

    companion object {
        private fun escapeType(type: String?) = type
            ?.replace(HybrisConstants.TS_JAVA_LANG_PREFIX, "")

        private fun flattenType(type: Type, elementType: String?) = when (type) {
            Type.COLLECTION -> "Collection<${elementType ?: '?'}>"
            Type.SET -> "Set<${elementType ?: '?'}>"
            Type.LIST -> "List<${elementType ?: '?'}>"
        }

        private fun mapCardinality(ref: TSMetaRelation.TSMetaRelationElement): String {
            val cardinality = ref.cardinality

            return when (ref.end) {
                TSMetaRelation.RelationEnd.SOURCE -> when (cardinality) {
                    Cardinality.ONE -> "1"
                    Cardinality.MANY -> "n"
                }

                TSMetaRelation.RelationEnd.TARGET -> when (cardinality) {
                    Cardinality.ONE -> "1"
                    Cardinality.MANY -> "m"
                }
            }
        }

        fun flattenType(plainType: String, allTypes: Map<String, TSTypedClassifier>): String? {
            var type = plainType
            val localized = type.startsWith(HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX, true)
            if (localized) {
                type = type.replace(HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX, "")
            }
            var flattenType = allTypes[type]?.flattenType ?: plainType
            flattenType = if (localized) HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX + flattenType
            else flattenType

            return escapeType(flattenType)
        }

        fun flattenType(meta: TSMetaRelation.TSMetaRelationElement) = if (meta.cardinality == Cardinality.ONE) escapeType(meta.type) ?: "?"
        else flattenType(meta.collectionType, escapeType(meta.type))

        fun flattenType(meta: TSGlobalMetaCollection) = flattenType(meta.type, escapeType(meta.elementType))
        fun flattenType(meta: TSGlobalMetaMap) = "Map<${escapeType(meta.argumentType) ?: '?'}, ${escapeType(meta.returnType) ?: '?'}>"
        fun flattenType(meta: TSGlobalMetaRelation) =
            escapeType(meta.source.type) + " [${mapCardinality(meta.source)}:${mapCardinality(meta.target)}] " + escapeType(meta.target.type)

        fun flattenType(meta: TSGlobalMetaAtomic) = meta.name
        fun flattenType(meta: TSGlobalMetaItem) = meta.name
        fun flattenType(meta: TSGlobalMetaEnum) = meta.name

        fun isDeprecated(dom: AttributeModel, name: String?) = dom.setters
            .any { name == it.name.stringValue && java.lang.Boolean.TRUE == it.deprecated.value }

        fun isLocalized(type: String?) = type?.startsWith(HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX, true)
            ?: false

        fun isDynamic(persistence: TSMetaPersistence) = PersistenceType.DYNAMIC == persistence.type

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

        fun getAllRelationEnds(
            metaModel: TSGlobalMetaModel,
            meta: TSGlobalMetaItem,
            extends: Set<TSGlobalMetaItem>
        ): Collection<TSMetaRelation.TSMetaRelationElement> {
            val currentMetaRelationEnds = getMetaRelationEnds(metaModel, meta)
            val extendsMetaRelationEnds = extends.flatMap { metaExtend -> getMetaRelationEnds(metaModel, metaExtend) }
            return currentMetaRelationEnds + extendsMetaRelationEnds
        }

        fun isAttributeDescriptor(it: TSGlobalMetaItem) = (HybrisConstants.TS_META_TYPE_ATTRIBUTE_DESCRIPTOR == it.name
            || it.allExtends.any { extends -> HybrisConstants.TS_META_TYPE_ATTRIBUTE_DESCRIPTOR == extends.name })


        private fun getMetaItem(metaModel: TSGlobalMetaModel, meta: TSGlobalMetaItem): TSGlobalMetaItem? {
            val realExtendedMetaItemName = meta.extendedMetaItemName ?: HybrisConstants.TS_TYPE_GENERIC_ITEM

            return metaModel.getMetaType<TSGlobalMetaItem>(TSMetaType.META_ITEM)[realExtendedMetaItemName]
        }

        private fun getMetaRelationEnds(metaModel: TSGlobalMetaModel, meta: TSGlobalMetaItem): Collection<TSMetaRelation.TSMetaRelationElement> {
            val name = meta.name ?: return emptyList()
            return metaModel.getRelations(name) ?: emptyList()
        }
    }
}