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
import sap.commerce.toolset.impex.psi.ImpExUserRightsFirstValueGroup
import sap.commerce.toolset.impex.psi.ImpExValueLine
import java.io.Serial

abstract class ImpExUserRightsFirstValueGroupMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExUserRightsFirstValueGroup {

    override fun getValueLine(): ImpExValueLine? = CachedValuesManager.getManager(project).getCachedValue(
        this, CACHE_KEY_VALUE_LINE, {
            val valueLine = PsiTreeUtil
                .getParentOfType(this, ImpExValueLine::class.java)

            CachedValueProvider.Result.createSingleDependency(
                valueLine,
                PsiModificationTracker.MODIFICATION_COUNT,
            )
        }, false
    )

    companion object {
        val CACHE_KEY_VALUE_LINE = Key.create<CachedValue<ImpExValueLine?>>("SAP_CX_IMPEX_VALUE_LINE")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}