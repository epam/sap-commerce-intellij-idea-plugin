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

package sap.commerce.toolset.flexibleSearch.psi

import sap.commerce.toolset.flexibleSearch.file.FlexibleSearchFile
import sap.commerce.toolset.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil

object FlexibleSearchElementFactory {

    fun createIdentifier(project: Project, name: String): PsiElement = createFile(project, name).firstChild

    fun createColumnSeparator(project: Project, separator: String): PsiElement? = createFile(project, "SELECT {alias${separator}name}")
        .let { PsiTreeUtil.findChildOfType(it, FlexibleSearchColumnSeparator::class.java) }
        ?.firstChild

    fun createFile(project: Project, text: String): FlexibleSearchFile = PsiFileFactory.getInstance(project)
        .createFileFromText(
            "dummy." + FlexibleSearchFileType.defaultExtension,
            FlexibleSearchFileType, text
        ) as FlexibleSearchFile
}