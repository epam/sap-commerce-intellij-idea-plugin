/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.flexibleSearch.injection

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.flexibleSearch.FxSUtils
import com.intellij.idea.plugin.hybris.impex.psi.ImpexString
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import java.util.*

class FlexibleSearchInjector : LanguageInjector {

    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        if (host is ImpexString) {
            val hostString = StringUtil.unquoteString(host.getText()).lowercase(Locale.getDefault())
            if (StringUtil.trim(hostString).startsWith(HybrisConstants.METHOD_SEARCH_NAME + ' ')) {
                registerInjectionPlace(injectionPlacesRegistrar, host)
            }
        } else {
            val hostParent = host.parent ?: return
            when (hostParent) {
                is PsiMethodCallExpression -> inject(hostParent, injectionPlacesRegistrar, host)
                else -> {
                    if (host is PsiLiteralExpression && hostParent !is PsiPolyadicExpression) {
                        inject(injectionPlacesRegistrar, host) { FxSUtils.computeExpression(host) }
                    }
                }
            }
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

    private fun inject(psi: PsiMethodCallExpression, injectionPlacesRegistrar: InjectedLanguagePlaces, host: PsiLanguageInjectionHost) {
        val method = psi.resolveMethod() ?: return
        if (HybrisConstants.METHOD_SEARCH_NAME != method.name) return
        val containingClass = method.containingClass ?: return
        if (HybrisConstants.CLASS_FLEXIBLE_SEARCH_SERVICE_NAME != containingClass.name) return

        registerInjectionPlace(injectionPlacesRegistrar, host)
    }

    private fun registerInjectionPlace(
        injectionPlacesRegistrar: InjectedLanguagePlaces,
        host: PsiElement,
        quoteLength: Int = 1
    ) {
        try {
            injectionPlacesRegistrar.addPlace(
                FlexibleSearchLanguage.INSTANCE,
                TextRange.from(quoteLength, host.textLength - (quoteLength * 2)),
                null,
                null
            )
        } catch (e: ProcessCanceledException) {
            // ignore
        } catch (e: Throwable) {
            LOG.error(e)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(FlexibleSearchInjector::class.java)
    }
}