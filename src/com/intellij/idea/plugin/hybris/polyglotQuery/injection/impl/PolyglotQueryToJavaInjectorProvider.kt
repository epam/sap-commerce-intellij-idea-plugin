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

package com.intellij.idea.plugin.hybris.polyglotQuery.injection.impl

import com.intellij.idea.plugin.hybris.lang.injection.impl.AbstractLanguageToJavaInjectorProvider
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiLiteralExpression

class PolyglotQueryToJavaInjectorProvider : AbstractLanguageToJavaInjectorProvider(PolyglotQueryLanguage.instance) {

    override fun computeExpression(host: PsiLiteralExpression) = PolyglotQueryUtils.computeExpression(host)
    override fun canProcess(expression: String) = PolyglotQueryUtils.isPolyglotQuery(expression)

    companion object {
        val instance: PolyglotQueryToJavaInjectorProvider? = ApplicationManager.getApplication().getService(PolyglotQueryToJavaInjectorProvider::class.java)
    }
}