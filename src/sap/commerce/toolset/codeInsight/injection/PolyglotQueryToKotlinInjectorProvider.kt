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

package sap.commerce.toolset.codeInsight.injection

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import sap.commerce.toolset.polyglotQuery.PolyglotQueryLanguage
import sap.commerce.toolset.polyglotQuery.PolyglotQueryUtils

@Service
class PolyglotQueryToKotlinInjectorProvider : LanguageToKotlinInjectorProvider(PolyglotQueryLanguage) {

    override fun canProcess(expression: String) = PolyglotQueryUtils.isPolyglotQuery(expression)

    companion object {
        fun getInstance(): PolyglotQueryToKotlinInjectorProvider = application.service()
    }

}