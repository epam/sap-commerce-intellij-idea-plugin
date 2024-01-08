/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexGroovyScriptBody
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

    private val groovyMarker = "#%groovy%"
    private val quoteSymbolLength = 1
    private val offset = "\"#%groovy%".count()

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

        LanguageInjectionUtil.tryInject(xmlFile, host, ScriptType.GROOVY) { length, offset ->
            injectLanguage(injectionPlacesRegistrar, length, offset)
        }
    }

    private fun handleImpex(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces) {
        when (host) {
            is ImpexString -> {
                val hostString = StringUtil.unquoteString(host.getText()).lowercase()
                if (StringUtil.trim(hostString).replaceFirst("\"", "").startsWith(groovyMarker)) {
                    val markerOffset = setOf("beforeeach:", "aftereach:", "if:")
                        .map { it to host.getText().indexOf(it, 0, true) }
                        .firstOrNull { it.second > -1 }
                        ?.let { it.first.length + it.second }
                        ?: offset

                    injectLanguage(injectionPlacesRegistrar, host.getTextLength() - markerOffset - quoteSymbolLength, markerOffset)
                } else if (LanguageInjectionUtil.getLanguageForInjection(host) == ScriptType.GROOVY) {
                    injectLanguage(injectionPlacesRegistrar, host.getTextLength() - quoteSymbolLength - 1, quoteSymbolLength)
                }
            }

            is ImpexGroovyScriptBody -> {
                injectLanguage(injectionPlacesRegistrar, host.getTextLength(), 0)
            }
        }
    }

    /**
     * Imports are taken from official docs: https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/640172cbde9149ab8eb818180544020a.html?locale=en-US.<br>
     * `line` is actually a Map returned by the de.hybris.platform.impex.jalo.AbstractCodeLine.
     */
    private fun injectLanguage(injectionPlacesRegistrar: InjectedLanguagePlaces, length: Int, offset: Int) {
        val language = GroovyLanguage
        try {
            injectionPlacesRegistrar.addPlace(
                language,
                TextRange.from(offset, length), """
                    import de.hybris.platform.core.*
                    import de.hybris.platform.core.model.user.* 
                    import de.hybris.platform.core.HybrisEnumValue
                    import de.hybris.platform.util.* 
                    import de.hybris.platform.impex.jalo.* 
                    import de.hybris.platform.jalo.*
                    import de.hybris.platform.jalo.c2l.Currency
                    import de.hybris.platform.jalo.c2l.* 
                    import de.hybris.platform.jalo.user.*
                    import de.hybris.platform.jalo.flexiblesearch.* 
                    import de.hybris.platform.jalo.product.ProductManager

                    def line = new java.util.HashMap<>();
                    def impex = new de.hybris.platform.impex.jalo.imp.ImpExImportReader(null);
                    
                """.trimIndent(), null
            )
        } catch (e: ProcessCanceledException) {
            // ignore
        } catch (e: Throwable) {
            LOG.error(e)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(GroovyLanguageInjector::class.java)
    }
}