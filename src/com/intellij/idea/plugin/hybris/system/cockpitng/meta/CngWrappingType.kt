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

package com.intellij.idea.plugin.hybris.system.cockpitng.meta

enum class CngWrappingType(val presentation: String, val type: String, val tail: String? = null) {
    RANGE("Range", "Range()"),
    LOCALIZED("Localized", "Localized()"),
    LOCALIZED_SIMPLE("Localized Simple", "LocalizedSimple()"),
    LIST("List", "List()"),
    REFERENCE("Reference", "Reference()"),
    FIXED_VALUES_REFERENCE("Fixed Values Reference", "FixedValuesReference()"),
    MULTI_REFERENCE_COLLECTION("Multi Reference", "MultiReference-COLLECTION()", "collection"),
    MULTI_REFERENCE_LIST("Multi Reference", "MultiReference-LIST()", "list"),
    MULTI_REFERENCE_SET("Multi Reference", "MultiReference-SET()", "set"),
    EXTENDED_MULTI_REFERENCE_COLLECTION("Extended Multi Reference", "ExtendedMultiReference-COLLECTION()", "collection"),
    EXTENDED_MULTI_REFERENCE_LIST("Extended Multi Reference", "ExtendedMultiReference-LIST()", "list"),
    EXTENDED_MULTI_REFERENCE_SET("Extended Multi Reference", "ExtendedMultiReference-SET()", "set"),
    ENUM_MULTI_REFERENCE_COLLECTION("Enum Multi Reference", "EnumMultiReference-COLLECTION()", "collection"),
    ENUM_MULTI_REFERENCE_LIST("Enum Multi Reference", "EnumMultiReference-LIST()", "list"),
    ENUM_MULTI_REFERENCE_SET("Enum Multi Reference", "EnumMultiReference-SET()", "set"),
}