/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.groovy.settings.state

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag
import sap.commerce.toolset.settings.state.SpringContextMode
import sap.commerce.toolset.settings.state.TransactionMode

@Tag("GroovySettings")
data class GroovySettingsState(
    @JvmField @OptionTag val enableActionsToolbar: Boolean = true,
    @JvmField @OptionTag val enableActionsToolbarForGroovyTest: Boolean = false,
    @JvmField @OptionTag val enableActionsToolbarForGroovyIdeConsole: Boolean = false,
    @JvmField @OptionTag val springContextMode: SpringContextMode = SpringContextMode.DISABLED,
    @JvmField @OptionTag val transactionMode: TransactionMode = TransactionMode.ROLLBACK,
    @JvmField @OptionTag val execMode: GroovyExecMode = GroovyExecMode.TEMPLATE,
    @JvmField @OptionTag val exceptionHandling: GroovyExecExceptionHandling = GroovyExecExceptionHandling.FULL_STACKTRACE,
) {
    fun mutable() = Mutable(
        enableActionsToolbar = enableActionsToolbar,
        enableActionsToolbarForGroovyTest = enableActionsToolbarForGroovyTest,
        enableActionsToolbarForGroovyIdeConsole = enableActionsToolbarForGroovyIdeConsole,
        springContextMode = springContextMode,
        transactionMode = transactionMode,
        execMode = execMode,
        exceptionHandling = exceptionHandling,
    )

    data class Mutable(
        var enableActionsToolbar: Boolean,
        var enableActionsToolbarForGroovyTest: Boolean,
        var enableActionsToolbarForGroovyIdeConsole: Boolean,
        var springContextMode: SpringContextMode,
        var transactionMode: TransactionMode,
        var execMode: GroovyExecMode,
        var exceptionHandling: GroovyExecExceptionHandling,
    ) {
        fun immutable() = GroovySettingsState(
            enableActionsToolbar = enableActionsToolbar,
            enableActionsToolbarForGroovyTest = enableActionsToolbarForGroovyTest,
            enableActionsToolbarForGroovyIdeConsole = enableActionsToolbarForGroovyIdeConsole,
            springContextMode = springContextMode,
            transactionMode = transactionMode,
            execMode = execMode,
            exceptionHandling = exceptionHandling,
        )
    }
}