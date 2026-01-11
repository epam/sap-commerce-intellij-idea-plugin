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

package sap.commerce.toolset.actionSystem

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

fun Project.triggerAction(
    actionId: String,
    place: String = ActionPlaces.UNKNOWN,
    uiKind: ActionUiKind = ActionUiKind.Companion.NONE,
    dataContextProvider: () -> DataContext = { SimpleDataContext.getProjectContext(this) }
) {
    val action = ActionManager.getInstance().getAction(actionId)
        ?.let {
            val event = AnActionEvent.createEvent(
                it, dataContextProvider.invoke(),
                null, place, uiKind, null
            );
            ActionUtil.performAction(it, event)
        }

    if (action == null) {
        thisLogger().warn("Could not find action $actionId")
    }
}

fun triggerAction(
    actionId: String,
    event: AnActionEvent,
) {
    val action = ActionManager.getInstance().getAction(actionId)
        ?.let { ActionUtil.performAction(it, event) }

    if (action == null) {
        logger<ActionManager>().warn("Could not find action $actionId")
    }
}
