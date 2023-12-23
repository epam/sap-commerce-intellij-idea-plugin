/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.actions

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement

abstract class AbstractGoToDeclarationAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    protected fun navigate(project: Project, dom: DomElement, offset: Int?) {
        if (offset == null) return

        dom.xmlTag
            ?.containingFile
            ?.virtualFile
            ?.let {
                PsiNavigationSupport.getInstance()
                    .createNavigatable(project, it, offset)
                    .navigate(false)
            }
    }
}