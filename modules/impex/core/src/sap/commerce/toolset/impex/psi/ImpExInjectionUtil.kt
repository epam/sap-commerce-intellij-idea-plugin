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

package sap.commerce.toolset.impex.psi

import com.intellij.psi.util.childrenOfType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.typeSystem.ScriptType

fun getScriptType(impexString: ImpExString): ScriptType? {
    val valueGroup = impexString
        .valueGroup
        ?: return null
    val fullHeaderParameter = valueGroup
        .fullHeaderParameter
        ?.takeIf { it.anyHeaderParameterName.textMatches("content") }
        ?: return null
    val header = fullHeaderParameter
        .headerLine
        ?.takeIf {
            it.fullHeaderType
                ?.headerTypeName
                ?.textMatches(HybrisConstants.TS_TYPE_SCRIPT)
                ?: false
        }
        ?: return null

    val scriptTypeColumn = header.getFullHeaderParameter("scriptType")
        ?: return ScriptType.GROOVY

    return valueGroup.valueLine
        ?.getValueGroup(scriptTypeColumn.columnNumber)
        ?.computeValue()
        ?.let { parseValue(it, scriptTypeColumn) }
        ?.let { ScriptType.byName(it) }
}

/**
 * It is also possible to use the following ImpEx:
 *
 * INSERT_UPDATE Script;code[unique=true]; content; scriptType(code,itemtype(code))[allownull=true]
 *                     ;some             ; some   ; GROOVY:ScriptType
 *
 * In such a case, we have to identify "code" parameter index, split value by ":" and take value of the "code" index.
 */
private fun parseValue(value: String, scriptTypeColumn: ImpExFullHeaderParameter) = scriptTypeColumn
    .childrenOfType<ImpExParameters>()
    .firstOrNull()
    ?.parameterList
    ?.map { it.text }
    ?.indexOf("code")
    ?.let { indexOfTheCodeParameter -> value.split(":").getOrNull(indexOfTheCodeParameter) }
    ?: value