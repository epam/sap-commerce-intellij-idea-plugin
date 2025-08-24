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

package com.intellij.idea.plugin.hybris.groovy.settings.state

import com.intellij.idea.plugin.hybris.tools.remote.execution.TransactionMode
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("GroovySettings")
data class GroovySettingsState(
    @JvmField @OptionTag val enableActionsToolbar: Boolean = true,
    @JvmField @OptionTag val enableActionsToolbarForGroovyTest: Boolean = false,
    @JvmField @OptionTag val enableActionsToolbarForGroovyIdeConsole: Boolean = false,
    @JvmField @OptionTag val txMode: TransactionMode = TransactionMode.ROLLBACK,
    @JvmField @OptionTag val timeOut: Int = HybrisHacHttpClient.DEFAULT_HAC_TIMEOUT / 1000,
    @JvmField @OptionTag val useScriptTemplate: Boolean = false,
    @JvmField @OptionTag val useCustomScriptTemplate: Boolean = false,
    @JvmField @OptionTag val customScriptTemplatePath: String = "",
    @JvmField @OptionTag val exceptionHandling: GroovyHACExceptionHandling = GroovyHACExceptionHandling.SIMPLE_STACKTRACE,
    @JvmField @OptionTag val exceptionHandlingName: String = GroovyHACExceptionHandling.SIMPLE_STACKTRACE.name,
) {
    fun mutable() = Mutable(
        enableActionsToolbar = enableActionsToolbar,
        enableActionsToolbarForGroovyTest = enableActionsToolbarForGroovyTest,
        enableActionsToolbarForGroovyIdeConsole = enableActionsToolbarForGroovyIdeConsole,
        txMode = txMode,
        timeOut = timeOut,
        useScriptTemplate = this@GroovySettingsState.useScriptTemplate,
        useCustomScriptTemplate = useCustomScriptTemplate,
        customScriptTemplatePath = customScriptTemplatePath,
        exceptionHandling = exceptionHandling,
        exceptionHandlingName = exceptionHandlingName,
    )

    data class Mutable(
        var enableActionsToolbar: Boolean,
        var enableActionsToolbarForGroovyTest: Boolean,
        var enableActionsToolbarForGroovyIdeConsole: Boolean,
        var txMode: TransactionMode,
        var timeOut: Int,
        var useScriptTemplate: Boolean,
        var useCustomScriptTemplate: Boolean,
        var customScriptTemplatePath: String,
        var exceptionHandling: GroovyHACExceptionHandling,
        var exceptionHandlingName: String,
        ) {
        fun immutable() = GroovySettingsState(
            enableActionsToolbar = enableActionsToolbar,
            enableActionsToolbarForGroovyTest = enableActionsToolbarForGroovyTest,
            enableActionsToolbarForGroovyIdeConsole = enableActionsToolbarForGroovyIdeConsole,
            txMode = txMode,
            timeOut = timeOut,
            useScriptTemplate = useScriptTemplate,
            useCustomScriptTemplate = useCustomScriptTemplate,
            customScriptTemplatePath = customScriptTemplatePath,
            exceptionHandling = exceptionHandling,
            exceptionHandlingName = exceptionHandlingName,
        )
    }
}