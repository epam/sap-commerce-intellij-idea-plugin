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
import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.properties.meta.CxPropertyRule

/**
 * Base of the inspections which hold a declaration against what SAP Commerce Cloud documents about the property.
 *
 * A subclass says which files it looks at, which rules it reports, and how each problem reads; everything else - walking
 * the file, matching the catalogue, highlighting the key rather than the whole line - is the same for all of them.
 */
abstract class CxPropertyInspection : LocalInspectionTool() {

    protected abstract val fileNames: Set<String>

    protected abstract fun rules(): Set<CxPropertyRule>
    protected abstract fun problem(key: String, rule: CxPropertyRule): String
    protected open fun fixes(key: String, rule: CxPropertyRule): Array<LocalQuickFix> = emptyArray()

    final override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : PsiElementVisitor() {

        override fun visitFile(file: PsiFile) {
            if (file.project.isNotHybrisProject) return
            if (file.name !in fileNames) return

            val propertiesFile = file.asSafely<PropertiesFile>() ?: return
            val reported = rules()

            propertiesFile.properties.forEach { property ->
                val key = property.key ?: return@forEach
                val rule = CxPropertyRule.of(key)?.takeIf { it in reported } ?: return@forEach

                holder.registerProblem(
                    property.psiElement,
                    keyRangeOf(property, key),
                    problem(key, rule),
                    *fixes(key, rule),
                )
            }
        }
    }

    /** Underlines the key alone - the value is rarely what is wrong, and the whole line is a lot of red. */
    private fun keyRangeOf(property: IProperty, key: String): TextRange {
        val element = property.psiElement
        val start = element.text.indexOf(key).takeIf { it >= 0 } ?: return TextRange(0, element.textLength)

        return TextRange(start, start + key.length)
    }
}
