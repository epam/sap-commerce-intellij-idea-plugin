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

package sap.commerce.toolset.cockpitNG.psi.reference

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.cockpitNG.psi.CngPsiHelper
import sap.commerce.toolset.psi.getValidResults
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker

class CngFlowTSItemReference(element: PsiElement) : CngTSItemReference(element) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        if (HybrisConstants.COCKPIT_NG_INITIALIZE_CONTEXT_TYPE.equals(value, true)) CachedValuesManager.getManager(element.project)
            .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
            .let { getValidResults(it) }
        else super.multiResolve(incompleteCode)

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, CngFlowTSItemReference>>("HYBRIS_CNGFLOWTSITEMREFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, CngFlowTSItemReference> { ref ->
            val element = ref.element
            val project = element.project

            val result: Array<ResolveResult> = CngPsiHelper.resolveContextTag(element)
                ?.getAttribute("type")
                ?.valueElement
                ?.navigationElement
                ?.let { arrayOf(PsiElementResolveResult(it)) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                result,
                TSModificationTracker.getInstance(project), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }
}
