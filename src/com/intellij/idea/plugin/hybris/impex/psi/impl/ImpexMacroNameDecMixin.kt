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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroNameDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.util.getKey
import com.intellij.idea.plugin.hybris.impex.psi.util.setName
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.*
import java.io.Serial

abstract class ImpexMacroNameDecMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpexMacroNameDec {

    override fun setName(newName: String): PsiElement = setName(this, newName)
    override fun getNameIdentifier() = this
    override fun getName() = getKey(node)
    override fun toString() = text
        ?: super.toString()

    override fun resolveValue(evaluatedMacroUsages: MutableSet<ImpexMacroUsageDec?>): String = CachedValuesManager.getManager(project).getCachedValue(
        this,
        Key.create<CachedValue<String>>("SAP_CX_IMPEX_RESOLVED_VALUE_" + evaluatedMacroUsages.size),
        {
            val resolvedValue = siblings(forward = true, withSelf = false)
                .map {
                    when (it) {
                        is ImpexMacroUsageDec -> {
                            if (evaluatedMacroUsages.contains(it)) return@map it.text

                            evaluatedMacroUsages.add(it)
                            it.resolveValue(evaluatedMacroUsages)
                                .let { value ->
                                    val ref = it.reference ?: return@let null
                                    value + ref.element.text.substringAfter(ref.canonicalText, "")
                                }
                                ?: it.text
                        }

                        else -> it.text
                    }
                }
                .joinToString("")
                .trim()
                .trimStart('=')
                .trimStart()

            CachedValueProvider.Result.createSingleDependency(
                resolvedValue,
                PsiModificationTracker.MODIFICATION_COUNT,
            )
        }, false
    )

    companion object {
        @Serial
        private val serialVersionUID: Long = 1984651966859085911L
    }
}
