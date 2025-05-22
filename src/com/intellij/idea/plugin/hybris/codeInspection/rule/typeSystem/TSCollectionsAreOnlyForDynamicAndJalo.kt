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

package com.intellij.idea.plugin.hybris.codeInspection.rule.typeSystem

import com.intellij.idea.plugin.hybris.codeInspection.fix.xml.XmlUpdateAttributeQuickFix
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class TSCollectionsAreOnlyForDynamicAndJalo : AbstractTSInspection() {

    override fun inspect(
        project: Project,
        dom: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.itemTypes.all
            .flatMap { it.attributes.attributes }
            .forEach { check(it, project, holder, severity) }
    }

    private fun check(
        attribute: Attribute,
        project: Project,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {

        if (!arrayOf(PersistenceType.DYNAMIC, PersistenceType.JALO).contains(attribute.persistence.type.value)) {
            project.service<TSMetaModelStateService>().get().getMetaCollection(attribute.type.stringValue)
                ?: return

            holder.createProblem(
                attribute.persistence.type,
                severity,
                attribute.qualifier.stringValue?.let { HybrisI18NBundleUtils.message("hybris.inspections.ts.CollectionsAreOnlyForDynamicAndJalo.details.key", it) }
                    ?: displayName,
                XmlUpdateAttributeQuickFix(
                    Persistence.TYPE,
                    PersistenceType.DYNAMIC.value
                )
            )
        }
    }
}