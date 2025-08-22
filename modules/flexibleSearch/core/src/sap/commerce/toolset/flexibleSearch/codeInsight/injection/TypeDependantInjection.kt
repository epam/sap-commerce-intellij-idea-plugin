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

package sap.commerce.toolset.flexibleSearch.codeInsight.injection

import sap.commerce.toolset.HybrisConstants

enum class TypeDependantInjection(val typeName: String, val parameterName: String, val targetTypeParameterName: String) {
    SEARCH_RESTRICTION(HybrisConstants.TS_TYPE_SEARCH_RESTRICTION, "query", "restrictedType"),
    AFTER_RETENTION_CLEANUP_RULE(HybrisConstants.TS_TYPE_AFTER_RETENTION_CLEANUP_RULE, "itemFilterExpression", "retirementItemType"), ;

    companion object {
        fun of(typeName: String): TypeDependantInjection? = entries.find { it.typeName == typeName }
    }
}
