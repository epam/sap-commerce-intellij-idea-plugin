/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.codeInspection

import com.intellij.codeInspection.LocalQuickFix
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.properties.codeInspection.fix.CxOpenPropertyDocumentationFix
import sap.commerce.toolset.properties.meta.CxPropertyRule

/**
 * Reports a property which the SAP Commerce Cloud build process writes itself being declared by the project.
 *
 * The documentation is blunt about the consequence — "Modifying these properties may cause your builds to fail" — so
 * this one is reported wherever the declaration lives, `local.properties` included.
 */
class CxManagedPropertyInspection : CxPropertyInspection() {

    override val fileNames = setOf(
        ProjectConstants.File.PROJECT_PROPERTIES,
        ProjectConstants.File.LOCAL_PROPERTIES,
    )

    override fun rules() = setOf(CxPropertyRule.MANAGED_BY_BUILD, CxPropertyRule.CLOUD_PORTAL_ONLY)

    override fun problem(key: String, rule: CxPropertyRule) = "'$key' is ${rule.title}"

    override fun fixes(key: String, rule: CxPropertyRule) = arrayOf<LocalQuickFix>(
        CxOpenPropertyDocumentationFix(rule.documentationUrl),
    )
}
