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
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.properties.codeInspection.fix.CxDeclarePropertyInLocalPropertiesFix
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyModel
import sap.commerce.toolset.properties.meta.CxPropertyPlaceholder

/**
 * Reports a `${...}` which the platform would never expand.
 *
 * Two ways that happens, and they read differently. A placeholder naming a property nothing declares is a typo or a
 * property someone forgot to add; a placeholder which leads back to its own property is a cycle, and the platform
 * gives up on it. Either way the literal `${...}` ends up in the value, which is rarely what anyone wanted.
 */
class CxUnresolvedPropertyPlaceholderInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : PsiElementVisitor() {

        override fun visitFile(file: PsiFile) {
            if (file.project.isNotHybrisProject) return

            val propertiesFile = file.asSafely<PropertiesFile>() ?: return
            val chain = CxPropertyCollector.getInstance(file.project).collect()
            if (chain.sources.none { it.file == file.originalFile.virtualFile }) return

            propertiesFile.properties.forEach { property ->
                val element = property.psiElement
                val value = property.value ?: return@forEach
                // The value sits at the end of the line, so a placeholder's offset within it has to be shifted by
                // however much of the line the key and the separator take up.
                val valueOffset = element.text.length - value.length

                CxPropertyPlaceholder.of(value).forEach { placeholder ->
                    val problem = problemOf(placeholder, property.key, chain) ?: return@forEach

                    holder.registerProblem(
                        element,
                        TextRange(valueOffset + placeholder.range.first, valueOffset + placeholder.range.last + 1),
                        problem.message,
                        *problem.fixes(placeholder.key),
                    )
                }
            }
        }
    }

    private fun problemOf(placeholder: CxPropertyPlaceholder, key: String?, chain: CxPropertyModel) = when {
        placeholder.key.isBlank() -> Problem.EMPTY
        placeholder.key !in chain -> Problem.UNDECLARED
        // Asking the chain is the only honest way to find a cycle: it is the thing which walks the references.
        key != null && chain.resolve(key)?.contains(CxPropertyPlaceholder.PREFIX + placeholder.key + CxPropertyPlaceholder.SUFFIX) == true -> Problem.CYCLE

        else -> null
    }

    private enum class Problem(val message: String) {
        EMPTY("Empty placeholder, which expands to nothing the platform can resolve"),
        UNDECLARED("No property of this project declares this key, so the placeholder is left as it is written"),
        CYCLE("This placeholder leads back to the property being declared, so the platform gives up expanding it"),
        ;

        fun fixes(key: String): Array<LocalQuickFix> = when (this) {
            UNDECLARED -> arrayOf(CxDeclarePropertyInLocalPropertiesFix(key))
            else -> emptyArray()
        }
    }
}
