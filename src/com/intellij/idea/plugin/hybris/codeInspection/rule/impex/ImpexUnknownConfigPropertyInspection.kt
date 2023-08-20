/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

package com.intellij.idea.plugin.hybris.codeInspection.rule.impex

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroValue
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.properties.PropertiesService
import com.intellij.lang.properties.PropertiesImplUtil
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class ImpexUnknownConfigPropertyInspection : LocalInspectionTool() {
    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = UnknownConfigPropertyVisitor(holder)
}


private class UnknownConfigPropertyVisitor(private val problemsHolder: ProblemsHolder) : ImpexVisitor() {
    private val cachedProperties = HashMap<String, Boolean>()

    override fun visitMacroUsageDec(usage: ImpexMacroUsageDec) {
        if (!usage.text.startsWith(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) return
        val propertyName = usage.text.substring(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX.length)

        if (propertyName.isNotEmpty()) {
            val isDeclarationExists = cachedProperties[propertyName]
            if (isDeclarationExists == true) return
            if (isDeclarationExists != null && isDeclarationExists == false) {
                problemsHolder.registerProblem(
                    usage,
                    message("hybris.inspections.impex.ImpexUnknownConfigPropertyInspection.param.key", propertyName),
                    ProblemHighlightType.ERROR
                )
            } else {
                val propertiesService = PropertiesService.getInstance(usage.project) ?: return
                val property = propertiesService.findMacroProperty(usage.project, propertyName)

                if (property == null) {
                    cachedProperties[propertyName] = false
                    problemsHolder.registerProblem(
                        usage,
                        message("hybris.inspections.impex.ImpexUnknownConfigPropertyInspection.param.key", propertyName),
                        ProblemHighlightType.ERROR
                    )
                } else {
                    cachedProperties[propertyName] = true
                }
            }
        }
    }

    override fun visitMacroDeclaration(declaration: ImpexMacroDeclaration) {
        val macroValue = PsiTreeUtil.findChildOfType(declaration, ImpexMacroValue::class.java)
        if (macroValue != null) {
            val prevLeaf = PsiTreeUtil.prevLeaf(macroValue)
            if (prevLeaf != null && prevLeaf.text.contains(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
                val key = macroValue.text
                val properties = PropertiesImplUtil.findPropertiesByKey(declaration.project, key)
                if (properties.isEmpty()) {
                    problemsHolder.registerProblem(
                        macroValue,
                        message("hybris.inspections.impex.ImpexUnknownConfigPropertyInspection.key", key),
                        ProblemHighlightType.ERROR
                    )
                }
            }
        }
    }

}