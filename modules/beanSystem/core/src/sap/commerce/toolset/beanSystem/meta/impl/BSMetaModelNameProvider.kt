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
package sap.commerce.toolset.beanSystem.meta.impl

import sap.commerce.toolset.beanSystem.model.*
import sap.commerce.toolset.beanSystem.model.Enum

object BSMetaModelNameProvider {

    fun extract(dom: Enum): String? = dom.clazz.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: EnumValue): String? = dom.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: Bean): String? = dom.clazz.stringValue
        ?.takeIf { it.isNotBlank() }
        ?.let { sap.commerce.toolset.beanSystem.meta.BSMetaHelper.getBeanName(it) }

    fun extract(dom: Hint): String? = dom.name.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: Property): String? = dom.name.stringValue?.takeIf { it.isNotBlank() }

}