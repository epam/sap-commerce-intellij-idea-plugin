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

package com.intellij.idea.plugin.hybris.util

import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

val PsiElement.isHybrisProject: Boolean
    get() = project.isHybrisProject

val PsiElement.isNotHybrisProject: Boolean
    get() = project.isNotHybrisProject

val Project.isHybrisProject: Boolean
    get() = ProjectSettingsComponent.getInstance(this).isHybrisProject()

val Project.isNotHybrisProject: Boolean
    get() = !isHybrisProject

val AnActionEvent.isHybrisProject: Boolean
    get() = this.dataContext.isHybrisProject

val DataContext.isHybrisProject: Boolean
    get() = this.getData(CommonDataKeys.PROJECT)?.isHybrisProject ?: false

fun <T> DataContext.ifHybrisProject(operation: () -> T): T? = if (isHybrisProject) operation() else null

fun <T> Project.ifHybrisProject(operation: () -> T): T? = if (isHybrisProject) operation() else null
