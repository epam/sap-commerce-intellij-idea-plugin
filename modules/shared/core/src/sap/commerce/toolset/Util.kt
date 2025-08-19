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

package sap.commerce.toolset

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.PropertyKey
import sap.commerce.toolset.settings.WorkspaceSettings

val PsiElement.isHybrisProject: Boolean
    get() = project.isHybrisProject

val PsiElement.isNotHybrisProject: Boolean
    get() = project.isNotHybrisProject

val Project.isHybrisProject: Boolean
    get() = WorkspaceSettings.getInstance(this).hybrisProject

val Project.isNotHybrisProject: Boolean
    get() = !isHybrisProject

val AnActionEvent.isHybrisProject: Boolean
    get() = this.dataContext.isHybrisProject

val DataContext.isHybrisProject: Boolean
    get() = this.getData(CommonDataKeys.PROJECT)?.isHybrisProject ?: false

fun <T> DataContext.ifHybrisProject(operation: () -> T): T? = if (isHybrisProject) operation() else null

fun <T> Project.ifHybrisProject(operation: () -> T): T? = if (isHybrisProject) operation() else null

infix fun <T> List<T>.equalsIgnoreOrder(other: List<T>) = this.size == other.size && this.toSet() == other.toSet()

fun i18n(
    @PropertyKey(resourceBundle = "i18n.HybrisBundle") key: String,
    vararg params: Any
) = HybrisI18NBundleUtils.message(key, *params)

fun i18nFallback(
    @PropertyKey(resourceBundle = "i18n.HybrisBundle") key: String,
    fallback: String,
    vararg params: Any
) = HybrisI18NBundleUtils.messageFallback(key, fallback, *params)