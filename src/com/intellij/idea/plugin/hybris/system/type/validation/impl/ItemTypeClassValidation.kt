/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import org.apache.commons.lang3.StringUtils

@Service
class ItemTypeClassValidation : AbstractClassesValidation<ItemType, Attribute>() {

    override fun buildGeneratedClassName(itemType: ItemType) = itemType.code.stringValue?.let { it + HybrisConstants.MODEL_SUFFIX }
        ?: StringUtils.EMPTY

    override fun buildJavaFieldName(itemAttribute: Attribute) = itemAttribute.qualifier.stringValue
        ?: StringUtils.EMPTY

    override fun getDefinedAttributes(itemType: ItemType): MutableList<Attribute> = itemType.attributes.attributes

    override fun isJavaClassGenerationDisabledForItemType(itemType: ItemType) = !itemType.generate.value
        || !itemType.model.generate.value

    override fun isJavaFieldGenerationDisabled(itemAttribute: Attribute) = !itemAttribute.generate.value
        || !itemAttribute.model.generate.value

    companion object {
        fun getInstance(): ItemTypeClassValidation = ApplicationManager.getApplication().getService(ItemTypeClassValidation::class.java)
    }
}
