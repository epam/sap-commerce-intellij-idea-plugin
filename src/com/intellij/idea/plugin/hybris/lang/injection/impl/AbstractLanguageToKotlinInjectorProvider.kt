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
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

abstract class AbstractLanguageToKotlinInjectorProvider(injectLanguage: Language) : AbstractLanguageInjectorProvider(injectLanguage) {

    override val language: Language = KotlinLanguage.INSTANCE

    abstract fun canProcess(expression: String): Boolean

    override fun tryInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        if (host !is KtStringTemplateExpression) return
        if (host.parent is KtBinaryExpression) return
        if (PsiTreeUtil.findChildOfType(host, KtReferenceExpression::class.java) != null) return

        val expression = host.text
        if (!canProcess(expression)) return

        if (expression.startsWith("\"\"\"")) {
            registerInjectionPlace(injectionPlacesRegistrar, host, 3)
        } else {
            registerInjectionPlace(injectionPlacesRegistrar, host, 1)
        }
    }

}