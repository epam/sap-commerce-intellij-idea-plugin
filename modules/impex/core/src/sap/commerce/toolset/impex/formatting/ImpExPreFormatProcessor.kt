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
package sap.commerce.toolset.impex.formatting

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.prevLeaf
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.psi.ImpExValueLine

class ImpExPreFormatProcessor : PreFormatProcessor {

    override fun process(element: ASTNode, range: TextRange) = element.psi
        ?.takeIf { it.language == ImpExLanguage }
        ?.takeIf { it.isValid }
        ?.findElementAt(range.endOffset)
        ?.let {
            if (it.elementType == ImpExTypes.CRLF) it.prevLeaf(true)
            else it
        }
        ?.let {
            it.parentOfType<ImpExValueLine>()
                ?.headerLine
                ?: it.parentOfType<ImpExHeaderLine>()
        }
        ?.tableRange
        ?: range
}
