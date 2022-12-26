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

package com.intellij.idea.plugin.hybris.system.bean.inspections.rules

import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class DuplicateEnumDefinition : AbstractBSInspection() {

    override fun inspect(
        project: Project,
        beans: Beans,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        if (beans.xmlElement == null) return

        beans.enums
            .forEach { inspect(it, holder, severity, project) }
    }

    private fun inspect(
        dom: Enum,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        val meta = BSMetaModelAccess.getInstance(project).findMetaForDom(dom) ?: return

        val currentFileDeclarations = meta.declarations
            .map { it.domAnchor }
            .filter { it.containingFile == dom.xmlElement!!.containingFile }
            .mapNotNull { it.retrieveDomElement() }
        if (currentFileDeclarations.size > 1) {
            holder.createProblem(
                dom.clazz,
                severity,
                displayName
            )
        }
    }
}