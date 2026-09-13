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

package sap.commerce.toolset.properties.codeInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import sap.commerce.toolset.isNotHybrisProject

/**
 * Base of the inspections which judge a property declaration by its key.
 *
 * A subclass says which files it has anything to say about and what is wrong with a declaration; walking the file,
 * staying out of projects which are not SAP Commerce, and underlining the key rather than the whole line are the
 * same for all of them.
 *
 * @param C what a file needs to be judged - the chain, the source it belongs to - worked out once per file rather than
 * once per declaration.
 */
abstract class CxPropertyDeclarationInspection<C : Any> : LocalInspectionTool() {

    /** The context [file] is judged in, or `null` when the inspection has nothing to say about it. */
    protected abstract fun prepare(file: PsiFile): C?

    /** A problem concerning the whole file, reported once instead of on each of its declarations. */
    protected open fun fileProblemOf(file: PsiFile, context: C): String? = null

    protected abstract fun problemOf(property: IProperty, file: PsiFile, context: C): CxPropertyProblem?

    final override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : PsiElementVisitor() {

        override fun visitFile(file: PsiFile) {
            if (file.project.isNotHybrisProject) return

            val propertiesFile = file.asSafely<PropertiesFile>() ?: return
            val context = prepare(file) ?: return

            fileProblemOf(file, context)?.let {
                holder.registerProblem(file, it)
                return
            }

            propertiesFile.properties.forEach { property ->
                val problem = problemOf(property, file, context) ?: return@forEach

                holder.registerProblem(
                    property.psiElement,
                    keyRangeOf(property),
                    problem.message,
                    *problem.fixes.toTypedArray(),
                )
            }
        }
    }

    /** Underlines the key alone - the value is rarely what is wrong, and the whole line is a lot of red. */
    private fun keyRangeOf(property: IProperty): TextRange {
        val element = property.psiElement
        val key = property.key ?: return TextRange(0, element.textLength)
        val start = element.text.indexOf(key).takeIf { it >= 0 } ?: return TextRange(0, element.textLength)

        return TextRange(start, start + key.length)
    }
}
