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
import com.intellij.psi.util.*
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.impex.psi.ImpExValueLine
import java.io.Serial

abstract class ImpExHeaderLineMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExHeaderLine {

    override fun getFullHeaderParameter(parameterName: String): ImpExFullHeaderParameter? = CachedValuesManager.getManager(project).getCachedValue(
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

    override fun getFullHeaderParameter(index: Int): ImpExFullHeaderParameter? = CachedValuesManager.getManager(project).getCachedValue(
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

    override fun getValueLines(): Collection<ImpExValueLine> = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_VALUE_LINES, {
        val subTypesIterator = siblings(withSelf = false).iterator()
        var proceed = true
        val valueLines = mutableListOf<ImpExValueLine>()

        while (proceed && subTypesIterator.hasNext()) {
            when (val psi = subTypesIterator.next()) {
                is ImpExHeaderLine -> proceed = false
                is ImpExValueLine -> valueLines.add(psi)
            }
        }
        CachedValueProvider.Result.createSingleDependency(
            valueLines,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    companion object {
        val CACHE_KEY_BY_INDEX = Key.create<CachedValue<Map<Int, ImpExFullHeaderParameter>>>("SAP_CX_IMPEX_FHP_BY_INDEX")
        val CACHE_KEY_BY_NAME = Key.create<CachedValue<Map<String, ImpExFullHeaderParameter>>>("SAP_CX_IMPEX_FHP_BY_NAME")
        val CACHE_KEY_VALUE_LINES = Key.create<CachedValue<Collection<ImpExValueLine>>>("SAP_CX_IMPEX_VALUE_LINES")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}