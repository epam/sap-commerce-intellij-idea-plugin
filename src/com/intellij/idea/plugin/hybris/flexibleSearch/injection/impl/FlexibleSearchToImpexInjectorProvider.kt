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
import com.intellij.idea.plugin.hybris.flexibleSearch.injection.FlexibleSearchInjectorProvider
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.psi.ImpexString
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.PsiLanguageInjectionHost
import java.util.*

class FlexibleSearchToImpexInjectorProvider : FlexibleSearchInjectorProvider() {

    override val language: Language = ImpexLanguage.getInstance()

    override fun tryInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        if (host is ImpexString) {
            val hostString = StringUtil.unquoteString(host.getText()).lowercase(Locale.getDefault())
            if (StringUtil.trim(hostString).startsWith(HybrisConstants.METHOD_SEARCH_NAME + ' ')) {
                registerInjectionPlace(injectionPlacesRegistrar, host)
            }
        }
    }

    companion object {
        val instance: FlexibleSearchInjectorProvider? = ApplicationManager.getApplication().getService(FlexibleSearchToImpexInjectorProvider::class.java)
    }
}