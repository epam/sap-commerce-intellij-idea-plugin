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
package com.intellij.idea.plugin.hybris.codeInspection.rule.typeSystem

import com.intellij.idea.plugin.hybris.system.type.model.Cardinality
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.Relation
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class TSDeploymentTableMustExistForManyToManyRelation : AbstractTSInspection() {

    override fun inspect(
        project: Project,
        dom: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.relations.relations.forEach { check(it, holder, severity) }
    }

    private fun check(
        dom: Relation,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val sourceCardinality = dom.sourceElement.cardinality.value ?: Cardinality.MANY
        val targetCardinality = dom.targetElement.cardinality.value ?: Cardinality.MANY

        if (sourceCardinality == Cardinality.MANY && targetCardinality == Cardinality.MANY && dom.deployment.typeCode.stringValue == null) {
            holder.createProblem(dom.deployment.typeCode, severity, displayName)
        }
    }
}