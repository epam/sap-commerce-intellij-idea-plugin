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
package com.intellij.idea.plugin.hybris.diagram.typeSystem

import com.intellij.diagram.DiagramBuilder
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.uml.core.actions.DiagramToolbarActionsProviderImpl
import com.intellij.util.application

class TSDiagramToolbarActionsProvider : DiagramToolbarActionsProviderImpl() {

    override fun addToolbarActionsTo(group: DefaultActionGroup, builder: DiagramBuilder) {
        ActionManager.getInstance().getAction("Diagram.Hybris.TypeSystem.Actions")
            ?.let {
                group.add(it)
                group.addSeparator()
            }

        super.addToolbarActionsTo(group, builder)
    }

    companion object {
        fun getInstance(): TSDiagramToolbarActionsProvider = application.service()
    }
}