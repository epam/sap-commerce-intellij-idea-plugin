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

package sap.commerce.toolset.logging

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import javax.swing.Icon

fun findPsiElement(project: Project, loggerIdentifier: String): PsiElement? = with(JavaPsiFacade.getInstance(project)) {
    findPackage(loggerIdentifier)
        ?: findClass(loggerIdentifier, GlobalSearchScope.allScope(project))
}

suspend fun getIcon(project: Project, loggerIdentifier: String): Icon? = readAction {
    findPsiElement(project, loggerIdentifier)?.getIcon(Iconable.ICON_FLAG_VISIBILITY or Iconable.ICON_FLAG_READ_STATUS)
}

suspend fun getPsiElementPointer(project: Project, loggerIdentifier: String): SmartPsiElementPointer<PsiElement>? = readAction {
    findPsiElement(project, loggerIdentifier)
        ?.let { SmartPointerManager.getInstance(it.project).createSmartPsiElementPointer(it) }
}