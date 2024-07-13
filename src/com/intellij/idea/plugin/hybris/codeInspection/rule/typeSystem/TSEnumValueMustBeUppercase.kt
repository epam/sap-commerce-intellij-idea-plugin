/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.EnumValue
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import java.util.*

class TSEnumValueMustBeUppercase : AbstractTSInspection() {

    override fun inspect(
        project: Project,
        dom: Items,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.enumTypes.enumTypes.forEach { enumType ->
            enumType.values.forEach { enumValue ->
                check(enumType, enumValue, holder, severity)
            }
        }
    }

    private fun check(
        enumType: EnumType,
        enumValue: EnumValue,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val enumName = enumType.code.stringValue ?: return
        val code = enumValue.code.stringValue ?: return
        if (StringUtil.isUpperCase(code)) return

        holder.createProblem(
            enumValue.code,
            severity,
            message("hybris.inspections.fix.ts.TSEnumValueMustBeUppercase.key", enumName, code),
            XmlUpdateAttributeQuickFix(EnumType.CODE, code.uppercase(Locale.ROOT))
        )
    }
}