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

import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaHint
import com.intellij.idea.plugin.hybris.system.bean.model.Hint
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class BSMetaHintImpl(
    dom: Hint,
    override val moduleName: String,
    override val extensionName: String,
    override val isCustom: Boolean,
    override val name: String?,
) : BSMetaHint {

    override val domAnchor: DomAnchor<Hint> = DomService.getInstance().createAnchor(dom)
    override val value = dom.stringValue

    override fun toString() = "Hint(module=$extensionName, name=$name, isCustom=$isCustom)"
}