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

package com.intellij.idea.plugin.hybris.system.bean.meta.model

import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomElement

interface BSMetaClassifier<DOM : DomElement> {

    val name: String?
        get() = null
    val module: Module
    val isCustom: Boolean
    val domAnchor: DomAnchor<DOM>
    fun retrieveDom(): DOM? = domAnchor.retrieveDomElement()
}

interface BSGlobalMetaClassifier<DOM : DomElement> : BSMetaClassifier<DOM> {
    val declarations: MutableSet<out BSMetaClassifier<DOM>>
    fun retrieveAllDoms(): List<DOM> = declarations
        .map { it.domAnchor }
        .mapNotNull { it.retrieveDomElement() }
}