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

package com.intellij.idea.plugin.hybris.system.bean.meta.model.impl

import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaAnnotations
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaHint
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaProperty
import com.intellij.idea.plugin.hybris.system.bean.model.Property
import com.intellij.idea.plugin.hybris.util.xml.toBoolean
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class BSMetaPropertyImpl(
    dom: Property,
    override val moduleName: String,
    override val extensionName: String,
    override val isCustom: Boolean,
    override val name: String?,
    override val annotations: List<BSMetaAnnotations>,
    override val hints: Map<String, BSMetaHint>,
) : BSMetaProperty {

    override val domAnchor: DomAnchor<Property> = DomService.getInstance().createAnchor(dom)
    override val type = dom.type.stringValue
    override val description = dom.description.stringValue
    override val isEquals = dom.equals.toBoolean()
    override val isDeprecated = dom.deprecated.toBoolean()
    override var flattenType: String? = BSMetaHelper.flattenType(this)
    override var referencedType: String? = BSMetaHelper.referencedType(this)

    override fun toString() = "Property(module=$extensionName, name=$name, isCustom=$isCustom)"
}