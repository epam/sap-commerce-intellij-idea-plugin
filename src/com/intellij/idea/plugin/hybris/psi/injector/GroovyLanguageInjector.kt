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
package com.intellij.idea.plugin.hybris.psi.injector

import com.intellij.idea.plugin.hybris.impex.psi.ImpexString
import com.intellij.idea.plugin.hybris.system.type.ScriptType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.XmlFile
import org.jetbrains.plugins.groovy.GroovyLanguage

class GroovyLanguageInjector : LanguageInjector {

    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        handleImpex(host, injectionPlacesRegistrar)
        handleBusinessProcess(host, injectionPlacesRegistrar)
    }

    private fun handleBusinessProcess(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces) {
        val xmlFile = host.containingFile as? XmlFile
            ?: return

        LanguageInjectionUtil.tryInject(xmlFile, host, ScriptType.GROOVY) { length, offset -> injectLanguage(injectionPlacesRegistrar, length, offset) }
    }

    private fun handleImpex(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces) {
        val impexString = host as? ImpexString
            ?: return

        val hostString = StringUtil.unquoteString(impexString.text).lowercase()
        if (StringUtil.trim(hostString).replaceFirst("\"", "").startsWith(GROOVY_MARKER)) {
            injectLanguage(injectionPlacesRegistrar, impexString.textLength - OFFSET - QUOTE_SYMBOL_LENGTH, OFFSET)
        } else if (LanguageInjectionUtil.getLanguageForInjection(impexString) == ScriptType.GROOVY) {
            injectLanguage(injectionPlacesRegistrar, impexString.textLength - QUOTE_SYMBOL_LENGTH - 1, QUOTE_SYMBOL_LENGTH)
        }
    }

    private fun injectLanguage(injectionPlacesRegistrar: InjectedLanguagePlaces, length: Int, offset: Int) {
        val language = GroovyLanguage
        try {
            injectionPlacesRegistrar.addPlace(
                language,
                TextRange.from(offset, length), null, null
            )
        } catch (e: ProcessCanceledException) {
            // ignore
        } catch (e: Throwable) {
            LOG.error(e)
        }
    }

    companion object {
        private const val GROOVY_MARKER = "#%groovy%"
        private const val QUOTE_SYMBOL_LENGTH = 1
        private val OFFSET = "\"#%groovy%".count()
        private val LOG = Logger.getInstance(GroovyLanguageInjector::class.java)
    }
}