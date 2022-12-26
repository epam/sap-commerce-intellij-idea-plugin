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

import com.intellij.idea.plugin.hybris.codeInspection.fix.XmlAddAttributeQuickFix
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class TSAttributeHandlerMustBeSetForDynamicAttribute : AbstractTSInspection() {

    override fun inspect(
        project: Project,
        items: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        items.itemTypes.all
            .flatMap { it.attributes.attributes }
            .forEach { check(it, holder, severity) }
    }

    private fun check(
        dom: Attribute,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val dynamic = PersistenceType.DYNAMIC == dom.persistence.type.value

        if (dynamic && dom.persistence.attributeHandler.stringValue == null) {
            holder.createProblem(
                dom.persistence,
                severity,
                displayName,
                XmlAddAttributeQuickFix(Persistence.ATTRIBUTE_HANDLER)
            )
        }
    }
}