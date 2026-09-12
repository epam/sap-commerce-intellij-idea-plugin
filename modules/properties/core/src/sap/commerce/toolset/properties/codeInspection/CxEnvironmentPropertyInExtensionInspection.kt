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
import sap.commerce.toolset.properties.codeInspection.fix.CxMovePropertyToLocalPropertiesFix
import sap.commerce.toolset.properties.codeInspection.fix.CxOpenPropertyDocumentationFix
import sap.commerce.toolset.properties.meta.CxPropertyRule

/**
 * Reports a property which SAP Commerce Cloud injects per environment being declared in an extension.
 *
 * A `project.properties` file is part of every build, of every environment, so a database URL or a storage account
 * written there hard-codes one environment into all of them. The deployed value wins regardless, which makes the
 * declaration both misleading to read and, for credentials, a secret committed to the repository.
 *
 * `local.properties` is where these belong: it is the developer's own machine, and it is not part of the build.
 */
class CxEnvironmentPropertyInExtensionInspection : CxPropertyInspection() {

    override val fileNames = setOf(ProjectConstants.File.PROJECT_PROPERTIES)

    override fun rules() = setOf(CxPropertyRule.MANAGED_BY_AUTOMATION)

    override fun problem(key: String, rule: CxPropertyRule) =
        "'$key' is ${rule.title}, declaring it in project.properties hard-codes one environment into every build"

    override fun fixes(key: String, rule: CxPropertyRule) = arrayOf<LocalQuickFix>(
        CxMovePropertyToLocalPropertiesFix(),
        CxOpenPropertyDocumentationFix(rule.documentationUrl),
    )
}
