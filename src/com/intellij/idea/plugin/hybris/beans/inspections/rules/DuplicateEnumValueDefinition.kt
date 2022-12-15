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

package com.intellij.idea.plugin.hybris.beans.inspections.rules

import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaModelAccess
import com.intellij.idea.plugin.hybris.beans.model.Bean
import com.intellij.idea.plugin.hybris.beans.model.Beans
import com.intellij.idea.plugin.hybris.beans.model.Enum
import com.intellij.idea.plugin.hybris.type.system.inspections.fix.XmlAddAttributeQuickFix
import com.intellij.idea.plugin.hybris.type.system.model.Persistence
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class DuplicateEnumValueDefinition : AbstractBeansInspection() {

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
        if (dom.values.isEmpty()) return

        val meta = BeansMetaModelAccess.getInstance(project).findMetaForDom(dom) ?: return

        dom.values.forEach { value ->
            val otherPropertyDeclarations = meta.declarations
                .map { it.values }
                .mapNotNull { it[value.stringValue] }

            if (otherPropertyDeclarations.size > 1) {
                holder.createProblem(
                    value,
                    severity,
                    displayName
                )
            }
        }
    }
}