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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueLine
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.util.*
import java.io.Serial

abstract class ImpexValueLineMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpexValueLine {

    override fun getHeaderLine(): ImpexHeaderLine? = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_HEADER_LINE, {
        val headerLine = PsiTreeUtil
            .getPrevSiblingOfType(this, ImpexHeaderLine::class.java)

        CachedValueProvider.Result.createSingleDependency(
            headerLine,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    companion object {
        val CACHE_KEY_HEADER_LINE = Key.create<CachedValue<ImpexHeaderLine?>>("SAP_CX_IMPEX_HEADER_LINE")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}