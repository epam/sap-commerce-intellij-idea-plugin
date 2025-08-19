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

package sap.commerce.toolset.beanSystem.codeInspection

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.beanSystem.model.Bean
import sap.commerce.toolset.beanSystem.model.Beans
import sap.commerce.toolset.beanSystem.model.Property
import sap.commerce.toolset.codeInspection.fix.XmlDeleteTagQuickFix

class BSDuplicateBeanPropertyDefinition : BSInspection() {

    override fun inspect(
        project: Project,
        dom: Beans,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.beans
            .forEach { inspect(it, holder, severity, project) }
    }

    private fun inspect(
        dom: Bean,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        if (dom.properties.isEmpty()) return

        val metas = BSMetaModelAccess.getInstance(project).findMetasForDom(dom)

        if (metas.isEmpty()) return

        val currentBeanProperties = dom.properties
                .filter{ it.name.stringValue != null }
                .groupBy { it.name.stringValue }

        dom.properties.forEach { property ->
            val otherPropertyDeclarations = metas
                .flatMap { it.declarations }
                .map { it.properties }
                .mapNotNull { it[property.name.stringValue] }

                if (otherPropertyDeclarations.size > 1 || (currentBeanProperties[property.name.stringValue]?.size ?: 1) > 1) {
                    createProblem(holder, property, severity, displayName)
            }
        }
    }

    private fun createProblem(
            holder: DomElementAnnotationHolder,
            property: Property,
            severity: HighlightSeverity,
            displayName: String
    ) = holder.createProblem(property.name, severity, displayName, XmlDeleteTagQuickFix())
}