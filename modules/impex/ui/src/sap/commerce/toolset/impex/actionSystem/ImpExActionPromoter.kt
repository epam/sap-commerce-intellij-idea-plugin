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
package sap.commerce.toolset.impex.actionSystem

import com.intellij.openapi.actionSystem.*
import org.jetbrains.annotations.Unmodifiable

class ImpExActionPromoter : ActionPromoter {

    override fun promote(actions: @Unmodifiable List<AnAction>, context: DataContext): @Unmodifiable List<AnAction> {
        CommonDataKeys.EDITOR.getData(context) ?: return actions

        val replace = actions.indexOfFirst { it.javaClass.simpleName == "CollapseRegionAction" }
            .takeIf { it != -1 }
            ?: return actions
        val impExCollapseRegionAction = ActionManager.getInstance().getAction("hybris.impex.collapseRegionAction")

        return actions.toMutableList().apply {
            set(replace, impExCollapseRegionAction)
        }
    }
}
