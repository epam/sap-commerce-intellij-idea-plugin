/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import java.io.Serial

abstract class ImpexHeaderLineMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpexHeaderLine {

    override fun getFullHeaderParameter(parameterName: String): ImpexFullHeaderParameter? = CachedValuesManager.getManager(project).getCachedValue(
        this, CACHE_KEY_BY_NAME,
        {
            val fhp = fullHeaderParameterList
                .associateBy { it.anyHeaderParameterName.text }

            CachedValueProvider.Result.createSingleDependency(
                fhp,
                PsiModificationTracker.MODIFICATION_COUNT,
            )

        },
        false
    )[parameterName]

    override fun getFullHeaderParameter(index: Int): ImpexFullHeaderParameter? = CachedValuesManager.getManager(project).getCachedValue(
        this, CACHE_KEY_BY_INDEX,
        {
            val fhp = fullHeaderParameterList
                .associateBy { it.columnNumber }

            CachedValueProvider.Result.createSingleDependency(
                fhp,
                PsiModificationTracker.MODIFICATION_COUNT,
            )

        },
        false
    )[index]

    companion object {
        val CACHE_KEY_BY_INDEX = Key.create<CachedValue<Map<Int, ImpexFullHeaderParameter>>>("SAP_CX_IMPEX_FHP_BY_INDEX")
        val CACHE_KEY_BY_NAME = Key.create<CachedValue<Map<String, ImpexFullHeaderParameter>>>("SAP_CX_IMPEX_FHP_BY_NAME")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}