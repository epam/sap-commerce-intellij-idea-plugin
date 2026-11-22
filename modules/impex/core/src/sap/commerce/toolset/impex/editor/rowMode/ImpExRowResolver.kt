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

package sap.commerce.toolset.impex.editor.rowMode

import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import sap.commerce.toolset.impex.psi.ImpExMacroDeclaration
import sap.commerce.toolset.impex.psi.ImpExValueLine

object ImpExRowResolver {

    fun resolve(valueLine: ImpExValueLine): ImpExRow? {
        val headerLine = valueLine.headerLine ?: return null
        val headerType = headerLine.fullHeaderType?.headerTypeName?.text ?: return null
        val subType = valueLine.subTypeName?.text

        // the lexer classifies macros based on the declarations above, so they have to be a part of the context,
        // the declarations are the direct children of the file, there is no need to walk the whole tree on each refresh
        val context = valueLine.containingFile.childrenOfType<ImpExMacroDeclaration>()
            .filter { it.textRange.endOffset <= valueLine.textRange.startOffset }
            .map { it.text }
            .plus(headerLine.text)
            .joinToString(separator = "\n", postfix = "\n")

        val groups = valueLine.valueGroupList
        val typeSystemReady = ImpExRowTypeSystem.isReady(valueLine.project)

        return ImpExRow(
            range = valueLine.textRange,
            headerType = headerType,
            subType = subType,
            contextPrefix = context,
            groupCount = groups.size,
            // a cell per column of the header, a group of the value line may be missing, the groups without a column are not edited
            cells = headerLine.fullHeaderParameterList.mapIndexed { index, parameter ->
                val group = groups.getOrNull(index)

                ImpExRowCell(
                    label = parameter.anyHeaderParameterName.text.trim().ifEmpty { "#${index + 1}" },
                    // the group may be missing, then the cell is placed at the end of the line
                    range = group?.valueRange ?: TextRange.from(valueLine.textRange.endOffset, 0),
                    unique = typeSystemReady && parameter.isUnique,
                    documentIdDeclaration = parameter.isDocumentIdDeclaration,
                    missingSeparators = if (group == null) index - groups.size + 1 else 0,
                )
            }
        )
    }
}
