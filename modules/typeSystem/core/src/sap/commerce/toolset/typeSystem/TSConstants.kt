/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.typeSystem

import sap.commerce.toolset.HybrisConstants

object TSConstants {

    const val ROOT_TAG_ITEMS_XML = "items"
    const val ROOT_TAG_DEPLOYMENT_MODEL_XML = "model"

    const val TYPECODE_MIN_ALLOWED = 10000
    val TYPECODE_RANGE_B2BCOMMERCE = TYPECODE_MIN_ALLOWED..10099
    val TYPECODE_RANGE_COMMONS = 13200..13299
    val TYPECODE_RANGE_XPRINT = 24400..24599
    val TYPECODE_RANGE_PRINT = 23400..23999
    val TYPECODE_RANGE_PROCESSING = 32700..32799

    const val MAX_RECURSION_LEVEL = 2
    const val JAVA_LANG_PREFIX = HybrisConstants.JAVA_LANG_PREFIX

    const val MODEL_SUFFIX = "Model"

    object Primitive {
        const val BYTE = "byte"
        const val SHORT = "short"
        const val INT = "int"
        const val LONG = "long"
        const val FLOAT = "float"
        const val DOUBLE = "double"
        const val CHAR = "char"
        const val BOOLEAN = "boolean"
    }

    object Type {
        const val OBJECT = "java.lang.Object"
        const val JAVA_CLASS = "java.lang.Class"
        const val ITEM = "Item"
        const val GENERIC_ITEM = "GenericItem"
        const val LOCALIZABLE_ITEM = "LocalizableItem"
        const val EXTENSIBLE_ITEM = "ExtensibleItem"
        const val SCRIPT = "Script"
        const val TRIGGER = "Trigger"
        const val CRON_JOB = "CronJob"
        const val CATALOG_VERSION = "CatalogVersion"
        const val LINK = "Link"
        const val SEARCH_RESTRICTION = "SearchRestriction"
        const val AFTER_RETENTION_CLEANUP_RULE = "AfterRetentionCleanupRule"
        const val ENUMERATION_VALUE = "EnumerationValue"
        const val ATTRIBUTE_DESCRIPTOR = "AttributeDescriptor"
        const val RELATION_DESCRIPTOR = "RelationDescriptor"
        const val USER = "User"
        const val USER_GROUP = "UserGroup"
        const val META_VIEW_TYPE = "ViewType"
        const val COMPOSED_TYPE = "ComposedType"

        val PRIMITIVES = setOf(Primitive.BYTE, Primitive.SHORT, Primitive.INT, Primitive.LONG, Primitive.FLOAT, Primitive.DOUBLE, Primitive.CHAR, Primitive.BOOLEAN)
    }

    object Attribute {
        const val TYPECODE_FIELD_NAME = "_TYPECODE"
        const val RELATION_ORDERING_POSTFIX = "POS"

        const val UNIQUE_KEY_ATTRIBUTE_QUALIFIER = "uniqueKeyAttributeQualifier"
        const val CATALOG_ITEM_TYPE = "catalogItemType"
        const val CATALOG_VERSION_ATTRIBUTE_QUALIFIER = "catalogVersionAttributeQualifier"

        const val SOURCE = "source"
        const val TARGET = "target"
        const val KEY = "key"
        const val VALUE = "value"
        const val CODE = "code"
        const val NAME = "name"
        const val PK = "pk"

        const val LOCALIZED_PREFIX = "localized:"
    }
}