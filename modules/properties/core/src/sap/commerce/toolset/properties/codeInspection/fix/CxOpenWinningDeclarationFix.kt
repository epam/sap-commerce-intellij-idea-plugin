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

package sap.commerce.toolset.properties.codeInspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.project.Project
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.psi.findPsi

/**
 * Opens the declaration which actually applies, so the overriding value can be read rather than hunted for.
 *
 * Navigating is not a fix in the sense of changing anything, but it is the action a reader wants first, and the
 * alternative - a gutter icon - is only visible when the line happens to be on screen.
 */
class CxOpenWinningDeclarationFix(private val key: String, private val presentableName: String) : LocalQuickFix {

    override fun getFamilyName() = "Open the declaration which applies"

    override fun getName() = "Open the declaration in $presentableName"

    override fun availableInBatchMode() = false

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val declaration = CxPropertyCollector.getInstance(project).collect()[key]?.declaration ?: return
        val file = declaration.source.file ?: return
        val offset = declaration.findPsi(project)?.psiElement?.textOffset ?: declaration.offset

        PsiNavigationSupport.getInstance()
            .createNavigatable(project, file, offset.coerceAtLeast(0))
            .navigate(true)
    }
}
