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

package sap.commerce.toolset.businessProcess.psi

import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.businessProcess.BpDomFileDescription
import sap.commerce.toolset.typeSystem.ScriptType

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