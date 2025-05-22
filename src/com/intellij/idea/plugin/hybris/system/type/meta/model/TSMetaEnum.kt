/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.system.type.meta.model

import com.intellij.idea.plugin.hybris.lang.documentation.renderer.hybrisDoc
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.EnumValue

interface TSMetaEnum : TSMetaClassifier<EnumType> {
    val values: Map<String, TSMetaEnumValue>
    val description: String?
    val jaloClass: String?
    val isAutoCreate: Boolean
    val isGenerate: Boolean
    val isDynamic: Boolean

    interface TSMetaEnumValue : TSMetaClassifier<EnumValue> {
        override val name: String
        val description: String?
    }
}

interface TSGlobalMetaEnum : TSMetaEnum, TSGlobalMetaClassifier<EnumType>, TSTypedClassifier {
    override val declarations: MutableSet<TSMetaEnum>

    override fun documentation() = hybrisDoc {
        title("Enum type", name ?: "?")
        subHeader(
            modifiersDocumentation(),
            description ?: "",
        )
    }.build()

    private fun modifiersDocumentation() = listOfNotNull(
        if (isAutoCreate) "autocreate" else null,
        if (isGenerate) "generate" else null,
        if (isDynamic) "dynamic" else null,
    ).joinToString(",&nbsp;")
}
