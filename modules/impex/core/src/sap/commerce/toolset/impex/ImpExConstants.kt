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

package sap.commerce.toolset.impex

import com.intellij.psi.tree.IFileElementType

object ImpExConstants {
    const val IMPEX_CONFIG_PREFIX = "\$config"
    const val IMPEX_CONFIG_COMPLETE_PREFIX = "$IMPEX_CONFIG_PREFIX-"
    const val IMPEX = "ImpEx"
    const val IMPEX_PREFIX_DOC_ID = "&"
    const val IMPEX_PREFIX_MACRO = "$"

    val FILE_NODE_TYPE = IFileElementType(ImpExLanguage)
}