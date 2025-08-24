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

package sap.commerce.toolset.impex.actionSystem

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValueGroup

class ImpExTableColumnMoveLeftAction : AbstractImpExTableColumnMoveAction(ImpExColumnPosition.LEFT) {

    init {
        with(templatePresentation) {
            text = "Move Column Left"
            description = "Move current column left"
            icon = HybrisIcons.ImpEx.Actions.MOVE_COLUMN_LEFT
        }
    }

    override fun isActionAllowed(project: Project, editor: Editor, element: PsiElement): Boolean {
        val suitableElement = getSuitableElement(element) ?: return false

        return when (suitableElement) {
            is ImpExFullHeaderParameter -> suitableElement.columnNumber > 0
            is ImpExValueGroup -> suitableElement.columnNumber > 0
            else -> false
        }
    }
}