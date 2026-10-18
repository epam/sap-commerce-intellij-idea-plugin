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
 * Holds every declaration of a property chain against the [CxPropertyRule] catalogue.
 *
 * This is the same judgement the editor inspections make, asked of the whole project at once instead of one open file
 * at a time - so a report and a highlighted line can never disagree about whether a declaration is a problem.
 */
object CxPropertyValidator {

    /**
     * Every declaration which breaks a rule, worst first and then by key.
     *
     * Only the tiers a rule names are looked at: a value the platform injects at runtime is not the project's doing,
     * however much it matches the catalogue.
     */
    fun validate(chain: CxPropertyModel): List<CxPropertyViolation> = chain.properties.values
        .flatMap { property ->
            val rule = CxPropertyRule.of(property.key) ?: return@flatMap emptyList()

            property.declarations
                .filter { it.source.scope in rule.scopes }
                .map { CxPropertyViolation(it, rule) }
        }
        .sortedWith(compareBy({ it.severity.ordinal }, { it.key }, { it.source.presentableName }))
}
