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

package com.intellij.idea.plugin.hybris.system.type.psi.reference.result

import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaMap
import com.intellij.idea.plugin.hybris.system.type.model.MapType

class MapResolveResult(
    val meta: TSMetaMap,
    private val navigateTo: String = MapType.CODE
) : TSReferenceBase.TSResolveResult {
    private val myDom: MapType? = meta.retrieveDom()
    override fun getElement() = myDom
        ?.let {
            when (navigateTo) {
                MapType.CODE -> it.code.xmlAttributeValue
                MapType.ARGUMENTTYPE -> it.argumentType.xmlAttributeValue
                MapType.RETURNTYPE -> it.returnType.xmlAttributeValue
                else -> null
            }
        }
    override fun isValidResult() = (myDom?.isValid ?: false) && element != null
}