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

import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelStateService
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.idea.plugin.hybris.system.type.model.all
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class TSDefaultValueForEnumTypeMustBeAssignable : AbstractTSInspection() {

    private val regex = Regex("em\\(\\)\\.getEnumerationValue\\(\\s*\".*\"\\s*,\\s*\".*\"\\s*\\)")

    override fun inspect(
        project: Project,
        dom: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.itemTypes.all
            .flatMap { it.attributes.attributes }
            .forEach { check(it, holder, severity, project) }
    }

    private fun check(
        dom: Attribute,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        project: Project
    ) {
        val defaultValue = dom.defaultValue.stringValue
            ?.trim()
            ?: return

        if (!defaultValue.startsWith("em().getEnumerationValue")) return

        // 1st validation:
        // Validate that default value for Enum is properly declared as 'em().getEnumerationValue("ENUM_TYPE", "ENUM_VALUE")'
        // some spaces are ignored
        if (!defaultValue.contains(regex)) {
            holder.createProblem(
                dom.defaultValue,
                severity,
                displayName
            )
            return
        }

        val splitDefaultValue = defaultValue.split("\"")

        // 2nd validation:
        // Ensure that Type set as default value is the same as one set for current Attribute
        if (!splitDefaultValue[1].equals(dom.type.stringValue, true)) {
            holder.createProblem(
                dom.defaultValue,
                severity,
                displayName
            )
            return
        }

        val meta = project.service<TSMetaModelStateService>().get().getMetaEnum(dom.type.stringValue)
            ?: return

        // 3rd validation:
        // Verify that Value set as default value exists in the system for Enum (will not handle dynamic enum values in the DB)
        if (!meta.values.containsKey(splitDefaultValue[3])) {
            holder.createProblem(
                dom.defaultValue,
                severity,
                displayName
            )
        }
    }
}