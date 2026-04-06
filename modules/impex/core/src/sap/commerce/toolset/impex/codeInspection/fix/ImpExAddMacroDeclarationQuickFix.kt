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

package sap.commerce.toolset.impex.codeInspection.fix

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.impex.psi.ImpExElementFactory
import sap.commerce.toolset.impex.psi.ImpExHeaderLine

class ImpExAddMacroDeclarationQuickFix(
    element: PsiElement,
    private val macroName: String
) : LocalQuickFixOnPsiElement(element) {

    override fun getFamilyName() = "[y] Missing macro declarations"
    override fun getText() = "Add macro declaration '$macroName'"

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val firstLeaf = startElement
            .parentOfType<ImpExHeaderLine>()
            ?: return

        ImpExElementFactory.createFile(
            project, """
            $macroName =  

        """.trimIndent()
        )
            .children
            .forEach { file.addBefore(it, firstLeaf) }
    }
}