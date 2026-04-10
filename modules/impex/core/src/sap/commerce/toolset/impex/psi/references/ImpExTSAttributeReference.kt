/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.impex.psi.references

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider
import sap.commerce.toolset.impex.psi.ImpExAnyHeaderParameterName
import sap.commerce.toolset.psi.getValidResults
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import sap.commerce.toolset.typeSystem.psi.reference.TSReferenceBase
import sap.commerce.toolset.typeSystem.psi.reference.result.TSResolveResultUtil

class ImpExTSAttributeReference(owner: ImpExAnyHeaderParameterName) : TSReferenceBase<ImpExAnyHeaderParameterName>(owner) {

    override fun resolve() = multiResolve(false).firstOrNull()?.element

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
            .let { getValidResults(it) }
    }

    companion object {
        @JvmStatic
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpExTSAttributeReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpExTSAttributeReference> { ref ->
            val project = ref.project
            val metaModelAccess = TSMetaModelAccess.getInstance(project)
            val featureName = ref.value
            val result = ref.element.headerItemTypeName
                ?.text
                ?.let { TSResolveResultUtil.tryResolveAttribute(metaModelAccess, featureName, it) }
                ?.let { arrayOf(it) }
                ?: ResolveResult.EMPTY_ARRAY

            // no need to track with PsiModificationTracker.MODIFICATION_COUNT due manual cache reset via custom Mixin
            CachedValueProvider.Result.create(
                result,
                TSModificationTracker.getInstance(project)
            )
        }
    }
}
