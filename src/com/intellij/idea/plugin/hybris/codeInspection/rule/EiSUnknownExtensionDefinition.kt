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

package com.intellij.idea.plugin.hybris.codeInspection.rule

import com.intellij.idea.plugin.hybris.system.extensionInfo.model.ExtensionInfo
import com.intellij.idea.plugin.hybris.system.extensionInfo.model.RequiresExtension
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class EiSUnknownExtensionDefinition : AbstractEiSInspection() {

    override fun inspect(
        project: Project,
        dom: ExtensionInfo,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        if (dom.xmlElement == null) return

        dom.extension.requiresExtensions
            .filterNotNull()
            .forEach { inspect(it, holder, severity, project) }
    }

    private fun inspect(
        dom: RequiresExtension,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
//        if (dom.values.isEmpty()) return
//
//        val meta = BSMetaModelAccess.getInstance(project).findMetaForDom(dom) ?: return
//
//        dom.values.forEach { value ->
//            val otherPropertyDeclarations = meta.declarations
//                .map { it.values }
//                .mapNotNull { it[value.stringValue] }
//
//            if (otherPropertyDeclarations.size > 1) {
//                holder.createProblem(
//                    value,
//                    severity,
//                    displayName
//                )
//            }
//        }
    }
}