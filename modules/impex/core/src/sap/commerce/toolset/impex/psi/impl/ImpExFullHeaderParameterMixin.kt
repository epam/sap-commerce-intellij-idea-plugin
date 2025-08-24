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

package sap.commerce.toolset.impex.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValueGroup
import sap.commerce.toolset.impex.utils.ImpExPsiUtils
import java.io.Serial

abstract class ImpExFullHeaderParameterMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExFullHeaderParameter {

    override fun getColumnNumber(): Int = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_COLUMN_NUMBER, {
        val columnNumber = ImpExPsiUtils.getColumnNumber(this)

        CachedValueProvider.Result.createSingleDependency(
            columnNumber,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    override fun getValueGroups(): List<ImpExValueGroup> = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_VALUE_GROUPS, {
        val valueGroups = this
            .headerLine
            ?.valueLines
            ?.mapNotNull { it.getValueGroup(this.columnNumber) }
            ?: emptyList()

        CachedValueProvider.Result.createSingleDependency(
            valueGroups,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    override fun getAttributeValue(attributeModifier: AttributeModifier, defaultValue: String): String = getAttribute(attributeModifier)
        ?.anyAttributeValue
        ?.text
        ?.trim()
        ?: defaultValue

    companion object {
        val CACHE_KEY_COLUMN_NUMBER = Key.create<CachedValue<Int>>("SAP_CX_IMPEX_COLUMN_NUMBER")
        val CACHE_KEY_VALUE_GROUPS = Key.create<CachedValue<List<ImpExValueGroup>>>("SAP_CX_IMPEX_VALUE_GROUPS")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}