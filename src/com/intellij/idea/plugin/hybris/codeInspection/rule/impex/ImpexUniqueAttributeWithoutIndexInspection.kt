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

package com.intellij.idea.plugin.hybris.codeInspection.rule.impex

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.idea.plugin.hybris.impex.psi.ImpexVisitor
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisI18NBundleUtils.message

class ImpexUniqueAttributeWithoutIndexInspection : LocalInspectionTool() {

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : ImpexVisitor() {

        override fun visitFullHeaderParameter(param: ImpexFullHeaderParameter) {
            val attribute = param.anyHeaderParameterName.text

            // no need to validate special parameters
            if (attribute.startsWith('@') || HybrisConstants.ATTRIBUTE_PK.equals(attribute, true)) return

            param.modifiersList
                .flatMap { it.attributeList }
                .asSequence()
                .filter { it.anyAttributeValue?.text == "true" }
                .filter { it.anyAttributeName.stringList.isEmpty() }
                .filter { it.anyAttributeName.firstChild == it.anyAttributeName.lastChild }
                .filter { it.anyAttributeName.text == "unique" }
                .firstOrNull()
                ?: return

            val typeName = PsiTreeUtil.getParentOfType(param, ImpexHeaderLine::class.java)
                ?.fullHeaderType
                ?.headerTypeName
                ?.text
                ?: return

            val hasIndex = TSMetaModelAccess.getInstance(param.project).findMetaItemByName(typeName)
                ?.allIndexes
                ?.flatMap { it.keys }
                ?.any { it.equals(attribute, true) }
                ?: true

            if (hasIndex) return

            holder.registerProblem(
                param,
                message("hybris.inspections.impex.ImpexUniqueAttributeWithoutIndexInspection.key", attribute, typeName)
            )
        }
    }
}