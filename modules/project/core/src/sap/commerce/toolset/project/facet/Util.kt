/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.facet

import com.intellij.facet.Facet
import com.intellij.facet.impl.FacetUtil
import com.intellij.openapi.util.JDOMExternalizable
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.WriteExternalException
import com.intellij.util.asSafely

val Facet<*>.configurationXmlTag
    get() = try {
        FacetUtil.saveFacetConfiguration(this.getConfiguration())
            ?.also { it.asSafely<JDOMExternalizable>()?.writeExternal(it) }
    } catch (_: WriteExternalException) {
        null
    }
        ?.let { JDOMUtil.write(it) }
