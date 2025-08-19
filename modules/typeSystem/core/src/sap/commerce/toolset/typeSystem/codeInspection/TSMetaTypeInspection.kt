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

package sap.commerce.toolset.typeSystem.codeInspection

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.model.Items

abstract class TSMetaTypeInspection(private val messageKey: String) : TSInspection() {

    protected abstract fun collectMetaTypes(dom: Items): Collection<GenericAttributeValue<String>>
    protected abstract fun isValidMetaType(meta: TSGlobalMetaItem): Boolean

    override fun inspect(
        project: Project,
        dom: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        collectMetaTypes(dom).forEach { check(it, holder, severity, project) }
    }

    private fun check(
        dom: GenericAttributeValue<String>,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        val name = dom.stringValue ?: return
        TSMetaModelStateService.state(project)
            .getMetaItem(name)
            ?.takeUnless { isValidMetaType(it) }
            ?: return

        holder.createProblem(
            dom,
            severity,
            i18n(messageKey, name)
        )
    }
}