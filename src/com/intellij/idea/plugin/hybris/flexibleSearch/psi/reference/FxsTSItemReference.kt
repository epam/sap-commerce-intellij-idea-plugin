/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.flexibleSearch.psi.reference

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchDefinedTableName
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.ItemResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationResolveResult
import com.intellij.openapi.util.Key
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class FxsTSItemReference(owner: FlexibleSearchDefinedTableName) : TSReferenceBase<FlexibleSearchDefinedTableName>(owner) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        val CACHE_KEY =
            Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxsTSItemReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxsTSItemReference> { ref ->
            val lookingForName = ref.element.text.replace("!", "")
            val modelAccess = TSMetaModelAccess.getInstance(ref.project)

            val result: Array<ResolveResult> = modelAccess.findMetaItemByName(lookingForName)
                ?.declarations
                ?.map { ItemResolveResult(it) }
                ?.toTypedArray()
                ?: modelAccess.findMetaEnumByName(lookingForName)
                    ?.let { arrayOf(EnumResolveResult(it)) }
                ?: modelAccess.findMetaRelationByName(lookingForName)
                    ?.let { arrayOf(RelationResolveResult(it)) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                modelAccess.getMetaModel(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}