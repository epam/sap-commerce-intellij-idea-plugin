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

package com.intellij.idea.plugin.hybris.flexibleSearch.injection.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.flexibleSearch.FxSUtils
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.idea.plugin.hybris.lang.injection.impl.AbstractLanguageInjectorProvider
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import java.util.*

@Service
class FlexibleSearchToImpexInjectorProvider : AbstractLanguageInjectorProvider(FlexibleSearchLanguage) {

    override val language: Language = ImpexLanguage

    override fun tryInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        val valueGroup: ImpexValueGroup?
        val expression: String
        val quoteLength: Int

        when (host) {
            is ImpexString -> {
                valueGroup = host.valueGroup
                expression = StringUtil.unquoteString(host.getText()).lowercase(Locale.getDefault())
                quoteLength = 1
            }

            is ImpexValue -> {
                if (host.childrenOfType<ImpexString>().isNotEmpty()) return
                valueGroup = host.valueGroup
                expression = host.getText().lowercase(Locale.getDefault())
                quoteLength = 0
            }

            else -> return
        }

        // do not inject executable statement
        if (expression.trim().startsWith("#%")) return

        val valueLine = valueGroup
            ?.valueLine

        valueLine
            ?.headerLine
            ?.takeIf {
                it.fullHeaderType
                    ?.headerTypeName
                    ?.textMatches(HybrisConstants.TS_TYPE_SEARCH_RESTRICTION)
                    ?: false
            }
            ?.let { tryInjectSearchRestriction(valueLine, it, injectionPlacesRegistrar, host, expression, quoteLength) }
            ?: injectSimple(injectionPlacesRegistrar, host, expression, quoteLength)
    }

    private fun injectSimple(
        injectionPlacesRegistrar: InjectedLanguagePlaces,
        host: PsiLanguageInjectionHost,
        expression: String,
        quoteLength: Int
    ) {
        if (FxSUtils.isFlexibleSearchQuery(expression)) {
            registerInjectionPlace(injectionPlacesRegistrar, host, quoteLength = quoteLength)
        }
    }

    private fun tryInjectSearchRestriction(
        valueLine: ImpexValueLine,
        headerLine: ImpexHeaderLine,
        injectionPlacesRegistrar: InjectedLanguagePlaces,
        host: PsiLanguageInjectionHost,
        expression: String,
        quoteLength: Int
    ) {
        // inject only into `query` column
        host.parentOfType<ImpexValueGroup>()
            ?.fullHeaderParameter
            ?.text
            ?.takeIf { it.equals("query", true) }
            ?: return

        val restrictedTypeParameter = headerLine.getFullHeaderParameter("restrictedType")
            ?: return injectSimple(injectionPlacesRegistrar, host, expression, quoteLength = quoteLength)
        val restrictedType = valueLine.getValueGroup(restrictedTypeParameter.columnNumber)
            ?.computeValue()
            ?: return injectSimple(injectionPlacesRegistrar, host, expression, quoteLength = quoteLength)

        val alias = DeveloperSettingsComponent.getInstance(host.project)
            .state
            .flexibleSearchSettings
            .fallbackToTableNameIfNoAliasProvided
            .takeIf { it }
            ?.let { "AS item" }
            ?: ""

        val prefix = "SELECT * FROM {${restrictedType} $alias} WHERE "
        registerInjectionPlace(injectionPlacesRegistrar, host, prefix = prefix, quoteLength = quoteLength)
    }

    companion object {
        fun getInstance(): FlexibleSearchToImpexInjectorProvider? = ApplicationManager.getApplication().getService(FlexibleSearchToImpexInjectorProvider::class.java)
    }
}