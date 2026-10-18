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

package sap.commerce.toolset.properties.meta

/**
 * A declaration the project should not be making, and what is wrong with it.
 *
 * @param declaration the offending declaration, carrying the file it lives in.
 * @param rule the catalogue entry it falls under.
 */
data class CxPropertyViolation(
    val declaration: CxPropertyDeclaration,
    val rule: CxPropertyRule,
) {

    val key get() = declaration.key
    val severity get() = rule.severity
    val source get() = declaration.source

    /** Reads the way the inspection which reports the same declaration in the editor reads. */
    val message
        get() = when (rule.severity) {
            CxPropertyRuleSeverity.ERROR ->
                "'$key' is ${rule.title}, declaring it in ${source.name} hard-codes one environment into every build"

            CxPropertyRuleSeverity.WARNING -> "'$key' is ${rule.title}"
        }
}
