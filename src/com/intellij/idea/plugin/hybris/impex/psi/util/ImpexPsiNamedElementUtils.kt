/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroNameDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
fun setName(element: PsiElement, newName: String): PsiElement {
    val keyNode = element.node.firstChildNode
    if (keyNode != null) {
        val property = when (element) {
            is ImpexMacroNameDec -> createMacrosDecElement(element.project, newName)
            is ImpexMacroUsageDec -> createMacrosUsageElement(element.project, newName)
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
            .createFileFromText(name, ImpexFileType.INSTANCE, text) as ImpexFile
}

fun createMacrosUsageElement(project: Project, text: String): PsiElement? {
    val impexFile = createFile(project, "\$dummy = $text")
    return impexFile.lastChild.lastChild
}

fun createMacrosDecElement(project: Project, text: String): PsiElement? {
    val impexFile = createFile(project, "$text = \$dummy")
    return impexFile.firstChild.firstChild
}


