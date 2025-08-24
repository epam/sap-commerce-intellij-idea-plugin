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

import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.XmlFile
import sap.commerce.toolset.businessProcess.psi.tryInject
import sap.commerce.toolset.impex.psi.ImpExString
import sap.commerce.toolset.impex.psi.getScriptType
import sap.commerce.toolset.typeSystem.ScriptType

class JavaScriptLanguageInjector : LanguageInjector {

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

        tryInject(xmlFile, host, ScriptType.JAVASCRIPT) { length, offset -> injectLanguage(injectionPlacesRegistrar, length, offset) }
    }

    private fun handleImpex(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces) {
        val impexString = host as? ImpExString
            ?: return

        if (getScriptType(impexString) == ScriptType.JAVASCRIPT) {
            injectLanguage(
                injectionPlacesRegistrar, impexString.textLength - QUOTE_SYMBOL_LENGTH - 1, QUOTE_SYMBOL_LENGTH
            )
        }
    }

    private fun injectLanguage(injectionPlacesRegistrar: InjectedLanguagePlaces, length: Int, offset: Int) {
        injectionPlacesRegistrar.addPlace(
            JavascriptLanguage,
            TextRange.from(offset, length), null, null
        )
    }

    companion object {
        private const val QUOTE_SYMBOL_LENGTH = 1
        private val LOG = Logger.getInstance(JavaScriptLanguageInjector::class.java)
    }
}
