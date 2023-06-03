/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.tools.remote.console.actions

import com.intellij.execution.ExecutionBundle
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisTabs
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAwareAction

class HybrisClearAllAction(val hybrisTabs: HybrisTabs) :
        DumbAwareAction(ExecutionBundle.message("clear.all.from.console.action.name"),
                "Clear the contents of the console", AllIcons.Actions.GC) {

    override fun update(e: AnActionEvent) {
        var enabled = hybrisTabs.activeConsole().contentSize > 0
        if (!enabled) {
            enabled = e.getData(LangDataKeys.CONSOLE_VIEW) != null
            val editor = e.getData(CommonDataKeys.EDITOR)
            if (editor != null && editor.document.textLength == 0) {
                enabled = false
            }
        }
        e.presentation.isEnabled = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
        hybrisTabs.activeConsole().clear()
    }
}
