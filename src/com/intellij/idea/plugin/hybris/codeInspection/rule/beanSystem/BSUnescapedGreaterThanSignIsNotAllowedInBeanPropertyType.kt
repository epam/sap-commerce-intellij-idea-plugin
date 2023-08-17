/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.codeInspection.rule.beanSystem

import com.intellij.idea.plugin.hybris.codeInspection.fix.xml.XmlUpdateAttributeQuickFix
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.idea.plugin.hybris.system.bean.model.Property
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class BSUnescapedGreaterThanSignIsNotAllowedInBeanPropertyType : AbstractBSInspection() {

    override fun inspect(
        project: Project,
        dom: Beans,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.beans
            .flatMap { it.properties }
            .forEach { inspect(it, holder, severity) }
    }

    private fun inspect(
        dom: Property,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val propertyName = dom.name.xmlAttributeValue?.value ?: return
        val propertyType = dom.type.xmlAttributeValue
            ?.value
            ?.takeIf { it.contains(HybrisConstants.BS_SIGN_GREATER_THAN) }
            ?: return

        holder.createProblem(
            dom.type,
            severity,
            HybrisI18NBundleUtils.message("hybris.inspections.bs.BSUnescapedGreaterThanSignIsNotAllowedInBeanPropertyType.message", propertyName),
            XmlUpdateAttributeQuickFix(
                Property.TYPE,
                propertyType
                    .replace(HybrisConstants.BS_SIGN_LESS_THAN, HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED)
                    .replace(HybrisConstants.BS_SIGN_GREATER_THAN, HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED)
            )
        )
    }

}