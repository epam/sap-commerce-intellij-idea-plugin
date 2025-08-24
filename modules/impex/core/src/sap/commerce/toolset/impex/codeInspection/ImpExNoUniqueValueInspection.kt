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
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.impex.psi.ImpExHeaderLine

class ImpExNoUniqueValueInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.WARNING
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = Visitor(holder)

    private class Visitor(private val problemsHolder: ProblemsHolder) : PsiElementVisitor() {

        override fun visitFile(file: PsiFile) {
            PsiTreeUtil.getChildrenOfType(file, ImpExHeaderLine::class.java)
                ?.filter { it.fullHeaderType != null }
                ?.groupBy { createHeaderKey(it) }
                ?.forEach { (_, headerLines) ->
                    val fullParametersList = fullParametersList(headerLines)
                    val keyAttrsList = keyAttributesList(fullParametersList)

                    if (keyAttrsList.isNotEmpty()) {
                        val notKeyAttrsList = notKeyAttributesList(fullParametersList)
                        val distinctCommonAttrsNames = notKeyAttrsList.map { it.text }.distinct()

                        val keyAttrsGroupedByName = fullParametersList.filter { keyAttrPredicate(it) }.groupBy { it.anyHeaderParameterName.text }

                        val dataMap = keyAttrsGroupedByName.entries
                            .associate { (name, attrs) ->
                                name to attrs
                                    .flatMap { it.valueGroups }
                                    .map { it.value }
                            }

                        if (distinctCommonAttrsNames.isEmpty()) {
                            val attrsNames = fullParametersList
                                .filter { keyAttrPredicate(it) }
                                .map { it.text }
                                .distinct()
                            createDataTable(dataMap, attrsNames, keyAttrsList).analyze(problemsHolder)
                        } else {
                            createDataTable(dataMap, distinctCommonAttrsNames, notKeyAttrsList).analyze(problemsHolder)
                        }
                    }
                }
        }

        private fun createHeaderKey(it: ImpExHeaderLine) =
            "${it.fullHeaderType?.text?.cleanWhitespaces()}|${keyAttrsName(it).joinToString { attr -> attr.cleanWhitespaces() }}"
    }
}

private fun String.cleanWhitespaces() = this.replace(" ", "")