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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexDocumentIdDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValue
import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import java.io.Serial

abstract class ImpexDocumentIdDecMixin(node: ASTNode) : ImpexPsiNamedElementMixin(node), ImpexDocumentIdDec {

    override fun getValues(): Map<String, Collection<ImpexValue>> = CachedValuesManager.getCachedValue(this) {
        val foundValues = (parentOfType<ImpexFullHeaderParameter>()
            ?.valueGroups
            ?.mapNotNull { it.value }
            ?.groupBy { it.text }
            ?: emptyMap())

        CachedValueProvider.Result.create(
            foundValues, PsiModificationTracker.MODIFICATION_COUNT
        )
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 8447559537730205946L
    }
}