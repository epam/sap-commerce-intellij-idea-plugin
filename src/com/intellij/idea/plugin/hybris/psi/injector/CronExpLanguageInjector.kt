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

import com.intellij.cron.CronExpLanguage
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexString
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValue
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.childrenOfType

class CronExpLanguageInjector : LanguageInjector {

    private val quoteSymbolLength = 1

    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        injectionPlacesRegistrar: InjectedLanguagePlaces
    ) {
        handleImpex(host, injectionPlacesRegistrar)
    }

    /**
     * INSERT_UPDATE Trigger; cronJob(code)[unique = true]; cronExpression; cronExpression
     *                      ; cronjobLogCleanupCronjob    ; 0 0 0/1 * * ? ; "0 0 0/1 * * ?"
     */
    private fun handleImpex(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces) {
        when (host) {
            is ImpexString -> {
                val valueGroup = host.valueGroup ?: return
                if (isTriggerCronExpression(valueGroup)) {
                    injectLanguage(injectionPlacesRegistrar, host.getTextLength() - quoteSymbolLength - 1, quoteSymbolLength)
                }
            }

            is ImpexValue -> {
                if (host.childrenOfType<ImpexString>().isNotEmpty()) return

                val valueGroup = host.valueGroup ?: return
                if (isTriggerCronExpression(valueGroup)) {
                    injectLanguage(injectionPlacesRegistrar, host.getTextLength(), 0)
                }
            }
        }
    }

    private fun isTriggerCronExpression(valueGroup: ImpexValueGroup): Boolean {
        val fullHeaderParameter = valueGroup
            .fullHeaderParameter
            ?.takeIf { it.anyHeaderParameterName.textMatches("cronExpression") }
            ?: return false
        fullHeaderParameter
            .headerLine
            ?.takeIf {
                it.fullHeaderType
                    ?.headerTypeName
                    ?.textMatches(HybrisConstants.TS_TYPE_TRIGGER)
                    ?: false
            }
            ?: return false

        return true
    }

    private fun injectLanguage(injectionPlacesRegistrar: InjectedLanguagePlaces, length: Int, offset: Int) {
        try {
            injectionPlacesRegistrar.addPlace(
                CronExpLanguage,
                TextRange.from(offset, length), null, null
            )
        } catch (e: ProcessCanceledException) {
            // ignore
        } catch (e: Throwable) {
            LOG.error(e)
        }
    }

    companion object {
        private val LOG by lazy { Logger.getInstance(GroovyLanguageInjector::class.java) }
    }
}
