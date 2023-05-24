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

package com.intellij.idea.plugin.hybris.psi.injector

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexString
import com.intellij.idea.plugin.hybris.system.businessProcess.BpDomFileDescription
import com.intellij.idea.plugin.hybris.system.type.ScriptType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager

object LanguageInjectionUtil {

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

        val scriptTypeColumn = header.getFullHeaderParameter("scriptType")
            ?: return ScriptType.GROOVY

        return valueGroup.valueLine
            ?.getValueGroup(scriptTypeColumn.columnNumber)
            ?.computeValue()
            ?.let { ScriptType.byName(it) }
    }

    fun tryInject(
        xmlFile: XmlFile,
        host: PsiLanguageInjectionHost,
        targetScriptType: ScriptType,
        inject: (Int, Int) -> Unit
    ) {
        if (DomManager.getDomManager(xmlFile.project).getDomFileDescription(xmlFile) !is BpDomFileDescription) return

        val scriptType = host.parentOfType<XmlTag>()
            ?.takeIf { it.name == "script" }
            ?.getAttribute("type")
            ?.value
            ?.let { ScriptType.byName(it) }

        if (scriptType == targetScriptType) {
            val cdataOpen = "<![CDATA["
            val offset: Int
            val length: Int
            if (host.text.contains(cdataOpen)) {
                offset = host.text.indexOf("<![CDATA[") + cdataOpen.length
                length = host.text.substringAfter(cdataOpen)
                    .substringBeforeLast("]]")
                    .length

                inject.invoke(length, offset)
            } else {
                offset = 0
                length = host.textLength
            }
            inject.invoke(length, offset)
        }
    }
}