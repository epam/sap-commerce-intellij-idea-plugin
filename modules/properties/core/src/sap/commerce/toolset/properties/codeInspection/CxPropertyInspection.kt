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

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.properties.IProperty
import com.intellij.psi.PsiFile
import sap.commerce.toolset.properties.meta.CxPropertyRule

/**
 * Base of the inspections which hold a declaration against what SAP Commerce Cloud documents about the property.
 *
 * A subclass says which rules it reports and how each problem reads; which files those rules belong to comes from the
 * catalogue itself.
 */
abstract class CxPropertyInspection : CxPropertyDeclarationInspection<Unit>() {

    protected abstract fun rules(): Set<CxPropertyRule>
    protected abstract fun problem(key: String, rule: CxPropertyRule): String
    protected open fun fixes(key: String, rule: CxPropertyRule): List<LocalQuickFix> = emptyList()

    /** The fixes for a problem in [file], for a subclass whose fixes depend on what the project has. */
    protected open fun fixes(key: String, rule: CxPropertyRule, file: PsiFile): List<LocalQuickFix> = fixes(key, rule)

    final override fun prepare(file: PsiFile) = Unit.takeIf { rules().any { file.name in it.fileNames } }

    final override fun problemOf(property: IProperty, file: PsiFile, context: Unit): CxPropertyProblem? {
        val key = property.key ?: return null
        // A rule reported by this inspection still only applies to the files it names.
        val rule = CxPropertyRule.of(key)
            ?.takeIf { it in rules() && file.name in it.fileNames }
            ?: return null

        return CxPropertyProblem(problem(key, rule), fixes(key, rule, file))
    }
}
