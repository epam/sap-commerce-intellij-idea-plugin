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

package com.intellij.idea.plugin.hybris.type.system.meta.model.impl

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomService

abstract class TSMetaEntityImpl<D : DomElement>(
    internal val myDom: D,
    private val myModule: Module,
    private val myProject: Project,
    private val myCustom: Boolean,
    private val myName: String? = null
) {
    private val myDomAnchor = DomService.getInstance().createAnchor(myDom)

    fun getDomAnchor(): DomAnchor<D> = myDomAnchor
    fun retrieveDom(): D? = myDomAnchor.retrieveDomElement()
}