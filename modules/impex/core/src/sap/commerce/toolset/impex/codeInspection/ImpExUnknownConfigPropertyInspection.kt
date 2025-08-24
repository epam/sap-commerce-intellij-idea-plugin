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
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.properties.PropertiesImplUtil
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.psi.ImpExMacroDeclaration
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpExMacroValue
import sap.commerce.toolset.impex.psi.ImpExVisitor
import sap.commerce.toolset.project.PropertyService

class ImpExUnknownConfigPropertyInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = UnknownConfigPropertyVisitor(holder)

    private class UnknownConfigPropertyVisitor(private val problemsHolder: ProblemsHolder) : ImpExVisitor() {
        private val cachedProperties = HashMap<String, Boolean>()

        override fun visitMacroUsageDec(usage: ImpExMacroUsageDec) {
            if (!usage.text.startsWith(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) return
            val propertyName = usage.text.substring(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length)

            if (propertyName.isNotEmpty()) {
                val isDeclarationExists = cachedProperties[propertyName]
                if (isDeclarationExists == true) return
                if (isDeclarationExists != null && isDeclarationExists == false) {
                    problemsHolder.registerProblem(
                        usage,
                        i18n("hybris.inspections.impex.ImpexUnknownConfigPropertyInspection.param.key", propertyName),
                        ProblemHighlightType.ERROR
                    )
                } else {
                    val property = PropertyService.getInstance(usage.project).findMacroProperty(propertyName)

                    if (property == null) {
                        cachedProperties[propertyName] = false
                        problemsHolder.registerProblem(
                            usage,
                            i18n("hybris.inspections.impex.ImpexUnknownConfigPropertyInspection.param.key", propertyName),
                            ProblemHighlightType.ERROR
                        )
                    } else {
                        cachedProperties[propertyName] = true
                    }
                }
            }
        }

        override fun visitMacroDeclaration(declaration: ImpExMacroDeclaration) {
            val macroValue = PsiTreeUtil.findChildOfType(declaration, ImpExMacroValue::class.java)
            if (macroValue != null) {
                val prevLeaf = PsiTreeUtil.prevLeaf(macroValue)
                if (prevLeaf != null && prevLeaf.text.contains(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
                    val key = macroValue.text
                    val properties = PropertiesImplUtil.findPropertiesByKey(declaration.project, key)
                    if (properties.isEmpty()) {
                        problemsHolder.registerProblem(
                            macroValue,
                            i18n("hybris.inspections.impex.ImpexUnknownConfigPropertyInspection.key", key),
                            ProblemHighlightType.ERROR
                        )
                    }
                }
            }
        }

    }
}
