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

package com.intellij.idea.plugin.hybris.impex.injection

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexString
import com.intellij.idea.plugin.hybris.system.type.ScriptType

object ImpexScriptLanguageInjectionValidator {

    fun getLanguageForInjection(impexString: ImpexString): ScriptType? {
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

        val scriptTypeColumn = header
            .fullHeaderParameterList
            .find { it.anyHeaderParameterName.textMatches("scriptType") }
            ?: return ScriptType.GROOVY

        return valueGroup.valueLine
            ?.getValueGroup(scriptTypeColumn.columnNumber)
            ?.value
            ?.text
            ?.trim()
            ?.let { ScriptType.byName(it) }
            ?: scriptTypeColumn
                .modifiersList
                .flatMap { it.attributeList }
                .find { it.anyAttributeName.textMatches("default") }
                ?.anyAttributeValue
                ?.stringList
                ?.firstOrNull()
                ?.text
                ?.replace("\"", "")
                ?.let { ScriptType.byName(it) }
    }
}