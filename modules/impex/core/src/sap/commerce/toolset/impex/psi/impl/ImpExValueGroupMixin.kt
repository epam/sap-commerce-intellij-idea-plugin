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
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.*
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExValueGroup
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.utils.ImpExPsiUtils
import java.io.Serial

abstract class ImpExValueGroupMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExValueGroup {

    override fun getValueLine(): ImpExValueLine? = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_VALUE_LINE, {
        val valueLine = PsiTreeUtil
            .getParentOfType(this, ImpExValueLine::class.java)

        CachedValueProvider.Result.createSingleDependency(
            valueLine,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    override fun getFullHeaderParameter(): ImpExFullHeaderParameter? = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_FULL_HEADER_PARAMETER, {
        val header = this.valueLine
            ?.headerLine
            ?.getFullHeaderParameter(this.columnNumber)

        CachedValueProvider.Result.createSingleDependency(
            header,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    override fun getColumnNumber(): Int = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_COLUMN_NUMBER, {
        val columnNumber = ImpExPsiUtils.getColumnNumber(this)

        CachedValueProvider.Result.createSingleDependency(
            columnNumber,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    override fun computeValue(): String? = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_VALUE_OR_DEFAULT, {
        val computedValue = this
            .value
            ?.text
            ?: this.fullHeaderParameter
                ?.getAttribute(AttributeModifier.DEFAULT)
                ?.anyAttributeValue
                ?.let {
                    it.stringList.firstOrNull()
                        ?.text
                        ?: it.text
                }

        val defaultValue = computedValue
            ?.let { StringUtil.unquoteString(it) }
            ?.trim()

        CachedValueProvider.Result.createSingleDependency(
            defaultValue,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    companion object {
        val CACHE_KEY_VALUE_LINE = Key.create<CachedValue<ImpExValueLine?>>("SAP_CX_IMPEX_VALUE_LINE")
        val CACHE_KEY_FULL_HEADER_PARAMETER = Key.create<CachedValue<ImpExFullHeaderParameter?>>("SAP_CX_IMPEX_FULL_HEADER_PARAMETER")
        val CACHE_KEY_COLUMN_NUMBER = Key.create<CachedValue<Int>>("SAP_CX_IMPEX_COLUMN_NUMBER")
        val CACHE_KEY_VALUE_OR_DEFAULT = Key.create<CachedValue<String>>("SAP_CX_IMPEX_VALUE_OR_DEFAULT")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}