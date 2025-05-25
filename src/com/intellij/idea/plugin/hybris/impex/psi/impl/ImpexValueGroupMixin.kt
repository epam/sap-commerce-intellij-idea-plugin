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

package com.intellij.idea.plugin.hybris.impex.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueGroup
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueLine
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.*
import java.io.Serial

abstract class ImpexValueGroupMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpexValueGroup {

    override fun getValueLine(): ImpexValueLine? = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_VALUE_LINE, {
        val valueLine = PsiTreeUtil
            .getParentOfType(this, ImpexValueLine::class.java)

        CachedValueProvider.Result.createSingleDependency(
            valueLine,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    override fun getFullHeaderParameter(): ImpexFullHeaderParameter? = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_FULL_HEADER_PARAMETER, {
        val header = this.valueLine
            ?.headerLine
            ?.getFullHeaderParameter(this.columnNumber)

        CachedValueProvider.Result.createSingleDependency(
            header,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    override fun getColumnNumber(): Int = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_COLUMN_NUMBER, {
        val columnNumber = ImpexPsiUtils.getColumnNumber(this)

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
        val CACHE_KEY_VALUE_LINE = Key.create<CachedValue<ImpexValueLine?>>("SAP_CX_IMPEX_VALUE_LINE")
        val CACHE_KEY_FULL_HEADER_PARAMETER = Key.create<CachedValue<ImpexFullHeaderParameter?>>("SAP_CX_IMPEX_FULL_HEADER_PARAMETER")
        val CACHE_KEY_COLUMN_NUMBER = Key.create<CachedValue<Int>>("SAP_CX_IMPEX_COLUMN_NUMBER")
        val CACHE_KEY_VALUE_OR_DEFAULT = Key.create<CachedValue<String>>("SAP_CX_IMPEX_VALUE_OR_DEFAULT")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}