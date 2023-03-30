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
package com.intellij.idea.plugin.hybris.system.type.validation.impl

import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.EnumValue
import com.intellij.openapi.application.ApplicationManager
import org.apache.commons.lang3.StringUtils

class EnumTypeClassValidation : AbstractClassesValidation<EnumType, EnumValue>() {

    override fun buildGeneratedClassName(itemType: EnumType) = itemType.code.stringValue
        ?: StringUtils.EMPTY

    override fun buildJavaFieldName(itemAttribute: EnumValue) = itemAttribute.code.stringValue
        ?: StringUtils.EMPTY

    override fun getDefinedAttributes(itemType: EnumType): MutableList<EnumValue> = itemType.values
    override fun isJavaClassGenerationDisabledForItemType(itemType: EnumType) = !itemType.generate.value
    override fun isJavaFieldGenerationDisabled(itemAttribute: EnumValue) = false

    companion object {
        val instance: EnumTypeClassValidation = ApplicationManager.getApplication().getService(EnumTypeClassValidation::class.java)
    }
}
