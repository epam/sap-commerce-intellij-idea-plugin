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

package sap.commerce.toolset.impex.codeInspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.properties.IProperty
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisI18NBundleUtils.message
import sap.commerce.toolset.impex.psi.ImpexAnyHeaderParameterName
import sap.commerce.toolset.impex.psi.ImpexMacroDeclaration
import sap.commerce.toolset.impex.psi.ImpexVisitor
import sap.commerce.toolset.impex.psi.references.ImpExHeaderAbbreviationReference

class ImpExIncompleteHeaderAbbreviationUsageInspection : LocalInspectionTool() {

    private val cachedMacros = mutableSetOf<String>()
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = ImpexHeaderLineVisitor(holder, cachedMacros)
    override fun inspectionStarted(session: LocalInspectionToolSession, isOnTheFly: Boolean) {
        cachedMacros.clear()

        PsiTreeUtil.findChildrenOfAnyType(session.file, ImpexMacroDeclaration::class.java)
            .map { it.firstChild.text }
            .let { cachedMacros.addAll(it) }
    }

    private class ImpexHeaderLineVisitor(private val problemsHolder: ProblemsHolder, private val cachedMacros: Set<String>) : ImpexVisitor() {

        override fun visitAnyHeaderParameterName(parameter: ImpexAnyHeaderParameterName) {
            val reference = parameter.reference as? ImpExHeaderAbbreviationReference ?: return

            val headerAbbreviationValue = reference
                .resolve()
                ?.asSafely<IProperty>()
                ?.value
                ?: return

            val missingExpectedMacros = headerAbbreviationValue.split("...", " ", "'", "\\\\", "]", "[", ":")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .filter { it.startsWith('$') }
                .distinct()
                .filterNot { cachedMacros.contains(it) }
                .takeIf { it.isNotEmpty() }
                ?: return


            problemsHolder.registerProblemForReference(
                reference,
                ProblemHighlightType.ERROR,
                message("hybris.inspections.impex.ImpExIncompleteHeaderAbbreviationUsageInspection.key", parameter.text, missingExpectedMacros.joinToString()),
            )
        }
    }
}