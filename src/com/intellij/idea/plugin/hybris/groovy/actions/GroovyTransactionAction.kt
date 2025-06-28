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
package com.intellij.idea.plugin.hybris.groovy.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.TransactionMode
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.CheckboxAction

open class GroovyTransactionAction(text: String, description: String, private val transactionMode: TransactionMode) : CheckboxAction(
    text, description, null
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        return DeveloperSettingsComponent.getInstance(project).state.groovySettings.txMode == transactionMode
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        DeveloperSettingsComponent.getInstance(project).state.groovySettings.txMode = transactionMode
    }
}

class GroovyRollbackTransactionAction : GroovyTransactionAction(
    message("hybris.groovy.actions.transaction.rollback"),
    message("hybris.groovy.actions.transaction.rollback.description"),
    TransactionMode.ROLLBACK
)

class GroovyCommitTransactionAction : GroovyTransactionAction(
    message("hybris.groovy.actions.transaction.commit"),
    message("hybris.groovy.actions.transaction.commit.description"),
    TransactionMode.COMMIT
)