/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.impex.psi.util

import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.jetbrains.rd.util.firstOrNull

fun setName(element: PsiElement, newName: String): PsiElement {
    val keyNode = element.node.firstChildNode
    if (keyNode != null) {
        val property = when (element) {
            is ImpexMacroNameDec -> createMacrosDecElement(element.project, newName)
            is ImpexMacroUsageDec -> createMacrosUsageElement(element.project, newName)
            is ImpexDocumentIdDec -> createDocumentIdDecElement(element.project, newName)
            is ImpexDocumentIdUsage -> createDocumentIdUsageElement(element.project, newName)
            else -> null
        }
        val newKeyNode = if (property == null) return element else property.node
        element.node.replaceChild(keyNode, newKeyNode!!)
    }
    return element
}

fun getKey(node: ASTNode) = node.findChildByType(ImpexTypes.VALUE)
    ?.text
    // IMPORTANT: Convert embedded escaped spaces to simple spaces
    ?.replace("\\\\ ", " ")
    ?: node.text

fun createFile(project: Project, text: String): ImpexFile {
    val name = "dummy.impex"
    return PsiFileFactory.getInstance(project)
        .createFileFromText(name, ImpexFileType, text) as ImpexFile
}

fun createMacrosUsageElement(project: Project, text: String): PsiElement? {
    val impexFile = createFile(project, "\$dummy = $text")
    return impexFile.lastChild.lastChild
}

fun createMacrosDecElement(project: Project, text: String): PsiElement? {
    val impexFile = createFile(project, "$text = \$dummy")
    return impexFile.firstChild.firstChild
}

fun createDocumentIdDecElement(project: Project, text: String): PsiElement? {
    val impexFile = createFile(
        project, """
        INSERT Cart; $text
    """.trimIndent()
    )
    return impexFile.getHeaderLines().firstOrNull()
        ?.key
        ?.fullHeaderParameterList
        ?.firstOrNull()
        ?.anyHeaderParameterName
        ?.documentIdDec
}


fun createDocumentIdUsageElement(project: Project, text: String): PsiElement? {
    val impexFile = createFile(
        project, """
        INSERT Address; owner($text)
    """.trimIndent()
    )
    return impexFile.getHeaderLines().firstOrNull()
        ?.key
        ?.fullHeaderParameterList
        ?.firstOrNull()
        ?.parametersList
        ?.firstOrNull()
        ?.parameterList
        ?.firstOrNull()
        ?.documentIdUsage
}


