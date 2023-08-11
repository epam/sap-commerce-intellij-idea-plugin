/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueGroup
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.idea.plugin.hybris.psi.util.PsiTreeUtilExt
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class ImpExTableColumnRemoveAction : AbstractImpExTableColumnAction() {

    init {
        with(templatePresentation) {
            text = "Remove Column"
            description = "Remove current column"
            icon = HybrisIcons.TABLE_COLUMN_REMOVE
        }
    }

    override fun actionPerformed(project: Project, editor: Editor, element: PsiElement) {
        when (element) {
            is ImpexFullHeaderParameter -> {
                ImpexPsiUtils.getColumnForHeader(element)
                    .forEach { it.delete() }
                PsiTreeUtilExt.getPrevSiblingOfElementType(element, ImpexTypes.PARAMETERS_SEPARATOR)
                    ?.delete()
                element.delete()
            }

            is ImpexValueGroup -> {
                val headerParameter = element.fullHeaderParameter ?: return
                actionPerformed(project, editor, headerParameter)
            }
        }
    }
}
