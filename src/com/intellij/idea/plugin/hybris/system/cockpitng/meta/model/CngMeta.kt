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
package com.intellij.idea.plugin.hybris.system.cockpitng.meta.model

import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomService

open class CngMeta<DOM : DomElement>(
    dom: DOM,
    val fileName: String,
    val custom: Boolean,
    val domAnchor: DomAnchor<DOM> = DomService.getInstance().createAnchor(dom),
) {
    fun retrieveDom(): DOM? = domAnchor.retrieveDomElement()

    override fun toString() = "Name: $fileName | custom: $custom"
}