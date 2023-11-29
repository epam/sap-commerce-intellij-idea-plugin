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

package com.intellij.idea.plugin.hybris.lang.injection.impl

import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost

abstract class AbstractLanguageInjectorProvider(private val injectLanguage: Language) {

    protected abstract val language: Language

    protected abstract fun tryInject(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces)

    /**
     * in case of null return we have to try another injectorProvider
     */
    fun inject(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces): Unit? {
        if (host.language != language) return null

        tryInject(host, injectionPlacesRegistrar)

        return Unit
    }

    protected fun registerInjectionPlace(
        injectionPlacesRegistrar: InjectedLanguagePlaces,
        host: PsiElement,
        quoteLength: Int = 1,
        prefix: String? = null,
        suffix: String? = null,
        textRange: TextRange = TextRange.from(quoteLength, host.textLength - (quoteLength * 2))
    ) {
        try {
            injectionPlacesRegistrar.addPlace(
                injectLanguage,
                textRange,
                prefix,
                suffix
            )
        } catch (e: ProcessCanceledException) {
            // ignore
        } catch (e: Throwable) {
            LOG.error(e)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(AbstractLanguageInjectorProvider::class.java)
    }
}