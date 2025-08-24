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

import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import sap.commerce.toolset.impex.psi.impl.ImpExStringImpl

class XmlToImpExLanguageInjector : LanguageInjector {

    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        if (host !is ImpExStringImpl) return

        val hostString = StringUtil.unquoteString(host.text).lowercase()
        if (StringUtil.trim(hostString).replaceFirst("\"", "").isXmlLike()) {
            val language = XMLLanguage.INSTANCE
            injectionPlacesRegistrar.addPlace(
                language,
                TextRange.from(QUOTE_SYMBOL_LENGTH, host.textLength - 2), null, null
            )
        }
    }

    /**
     * return true if the String passed in is something like XML
     *
     * @return true of the string is XML, false otherwise
     */
    private val xmlPatternRegExp = "<(\\S+?)(.*?)>(.*?)</\\1>".toRegex()

    private fun String.isXmlLike(): Boolean {
        if (this.trim { it <= ' ' }.isNotEmpty()) {
            if (this.trim { it <= ' ' }.startsWith("<")) {
                return xmlPatternRegExp.containsMatchIn(this)
            }
        }

        return false
    }

    companion object {
        private const val QUOTE_SYMBOL_LENGTH = 1
    }
}