/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.flexibleSearch.injection.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.flexibleSearch.FxSUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.injection.FlexibleSearchInjectorProvider
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.*

class FlexibleSearchToJavaInjectorProvider : FlexibleSearchInjectorProvider() {

    override val language: Language = JavaLanguage.INSTANCE

    override fun tryInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        val hostParent = host.parent ?: return

        if (host is PsiLiteralExpression && hostParent !is PsiPolyadicExpression) {
            inject(injectionPlacesRegistrar, host) { FxSUtils.computeExpression(host) }
        } else if (hostParent is PsiExpressionList) {
            val psiMethod = hostParent.parent as? PsiMethodCallExpression ?: return
            inject(psiMethod, injectionPlacesRegistrar, host)
        }
    }

    private fun inject(
        injectionPlacesRegistrar: InjectedLanguagePlaces,
        host: PsiLiteralExpression,
        expressionProvider: () -> String
    ) {
        val expression = expressionProvider.invoke()
        if (!FxSUtils.isFlexibleSearchQuery(expression)) return

        val quoteLength = if (host.isTextBlock) 3 else 1
        registerInjectionPlace(injectionPlacesRegistrar, host, quoteLength)
    }

    private fun inject(
        psi: PsiMethodCallExpression,
        injectionPlacesRegistrar: InjectedLanguagePlaces,
        host: PsiLanguageInjectionHost
    ) {
        val method = psi.resolveMethod() ?: return
        if (HybrisConstants.METHOD_SEARCH_NAME != method.name) return
        val containingClass = method.containingClass ?: return
        if (HybrisConstants.CLASS_FLEXIBLE_SEARCH_SERVICE_NAME != containingClass.name) return

        registerInjectionPlace(injectionPlacesRegistrar, host)
    }

    companion object {
        val instance: FlexibleSearchInjectorProvider? = ApplicationManager.getApplication().getService(FlexibleSearchToJavaInjectorProvider::class.java)
    }
}