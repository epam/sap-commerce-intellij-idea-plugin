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

package com.intellij.idea.plugin.hybris.system.extensioninfo.file

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.ResolvingConverter

private val BOOLEAN_KEYS = listOf(
        HybrisConstants.EXTENSION_META_KEY_DEPRECATED,
        HybrisConstants.EXTENSION_META_KEY_HAC_MODULE,
        HybrisConstants.EXTENSION_META_KEY_BACKOFFICE_MODULE,
        HybrisConstants.EXTENSION_META_KEY_EXT_GEN
)

class MetaValueConverter : ResolvingConverter<String>() {

    override fun toString(t: String?, context: ConvertContext?) = t
    override fun fromString(s: String?, context: ConvertContext?) = s
    override fun getVariants(context: ConvertContext) = context.referenceXmlElement
            ?.parentsOfType<XmlTag>()
            ?.firstOrNull { it.localName == "meta" }
            ?.getAttributeValue("key")
            ?.let {
                if (BOOLEAN_KEYS.contains(it)) listOf("true", "false")
                else emptyList()
            }
            ?: emptyList()

}
