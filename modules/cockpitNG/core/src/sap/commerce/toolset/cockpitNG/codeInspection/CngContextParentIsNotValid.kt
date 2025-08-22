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

package sap.commerce.toolset.cockpitNG.codeInspection

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import sap.commerce.toolset.cockpitNG.meta.CngMetaModelStateService
import sap.commerce.toolset.cockpitNG.model.config.Config
import sap.commerce.toolset.cockpitNG.model.config.Context
import sap.commerce.toolset.i18n

class CngContextParentIsNotValid : CngConfigInspection() {

    override fun inspect(
        project: Project,
        dom: Config,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.contexts
            .forEach { check(it, holder, severity, project) }
    }

    private fun check(
        dom: Context,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        val mergeBy = dom.mergeBy.stringValue ?: return
        if (mergeBy == "type") return

        val parentValue = dom.parentAttribute.stringValue ?: return
        if (parentValue == Context.PARENT_AUTO) return

        val allowedValues = CngMetaModelStateService.state(project).contextAttributes[mergeBy]

        if (allowedValues == null || !allowedValues.contains(parentValue)) {
            holder.createProblem(
                dom.parentAttribute,
                severity,
                i18n("hybris.inspections.fix.cng.ContextParentIsNotValid.message", parentValue, mergeBy)
            )
        }
    }
}