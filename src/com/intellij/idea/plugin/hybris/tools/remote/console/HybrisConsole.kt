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

package com.intellij.idea.plugin.hybris.tools.remote.console

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager
import com.intellij.util.ui.JBUI
import java.io.Serial
import javax.swing.Icon

abstract class HybrisConsole(project: Project, title: String, language: Language) : LanguageConsoleImpl(project, title, language) {

    protected val borders10 = JBUI.Borders.empty(10)
    protected val borders5 = JBUI.Borders.empty(5, 10)
    protected val bordersLabel = JBUI.Borders.empty(10, 10, 10, 0)

    init {
        this.printDefaultText()
    }

    abstract fun execute(query: String): HybrisHttpResult

    abstract fun title(): String

    abstract fun tip(): String

    open fun icon(): Icon? = null

    open fun printDefaultText() = setInputText("")

    open fun connectionType() = HybrisRemoteConnectionSettings.Type.Hybris

    open fun onSelection() {
        //NOP
    }

    override fun dispose() {
        LineStatusTrackerManager.getInstance(project).releaseTrackerFor(editorDocument, consoleEditor)
        super.dispose()
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -2700270816491881103L
    }

}


