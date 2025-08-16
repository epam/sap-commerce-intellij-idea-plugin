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

package sap.commerce.toolset.codeInspection.rule.beanSystem

import sap.commerce.toolset.codeInspection.fix.xml.XmlUpdateAttributeQuickFix
import sap.commerce.toolset.system.bean.model.Beans
import sap.commerce.toolset.system.bean.model.Property
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisI18NBundleUtils

class BSUnescapedLessThanSignIsNotAllowedInBeanPropertyType : AbstractBSInspection() {

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
            ?.takeIf { it.contains(HybrisConstants.BS_SIGN_LESS_THAN) }
            ?: return

        holder.createProblem(
            dom.type,
            severity,
            HybrisI18NBundleUtils.message("hybris.inspections.bs.BSUnescapedLessThanSignIsNotAllowedInBeanPropertyType.message", propertyName),
            XmlUpdateAttributeQuickFix(
                Property.TYPE,
                propertyType
                    .replace(HybrisConstants.BS_SIGN_LESS_THAN, HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED)
                    .replace(HybrisConstants.BS_SIGN_GREATER_THAN, HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED)
            )
        )
    }

}