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
import com.intellij.util.asSafely
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.impex.psi.ImpExValueLine
import java.io.Serial

abstract class ImpExValueLineMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExValueLine {

    override fun getHeaderLine(): ImpExHeaderLine? = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_HEADER_LINE, {
        val headerLine = this.containingFile
            .asSafely<ImpExFile>()
            ?.getHeaderLines()
            ?.entries
            ?.find { it.value.contains(this) }
            ?.key

        CachedValueProvider.Result.createSingleDependency(
            headerLine,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    companion object {
        val CACHE_KEY_HEADER_LINE = Key.create<CachedValue<ImpExHeaderLine?>>("SAP_CX_IMPEX_HEADER_LINE")

        @Serial
        private val serialVersionUID: Long = -4491471414641409161L
    }
}