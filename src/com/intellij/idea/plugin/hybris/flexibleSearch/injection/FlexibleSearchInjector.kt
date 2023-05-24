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
package com.intellij.idea.plugin.hybris.flexibleSearch.injection

import com.intellij.idea.plugin.hybris.flexibleSearch.injection.impl.FlexibleSearchToImpexInjectorProvider
import com.intellij.idea.plugin.hybris.flexibleSearch.injection.impl.FlexibleSearchToJavaInjectorProvider
import com.intellij.idea.plugin.hybris.flexibleSearch.injection.impl.FlexibleSearchToKotlinInjectorProvider
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost

class FlexibleSearchInjector : LanguageInjector {

    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        FlexibleSearchToImpexInjectorProvider.instance
            ?.inject(host, injectionPlacesRegistrar)
            ?: FlexibleSearchToJavaInjectorProvider.instance
                ?.inject(host, injectionPlacesRegistrar)
            ?: FlexibleSearchToKotlinInjectorProvider.instance
                ?.inject(host, injectionPlacesRegistrar)
    }

}