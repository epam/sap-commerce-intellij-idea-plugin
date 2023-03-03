/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.impex.psi.references

import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.AttributeResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationEndResolveResult
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import com.intellij.psi.xml.XmlTag

class FunctionTSAttributeReference(owner: ImpexParameter) : TSReferenceBase<ImpexParameter>(owner) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        return CachedValuesManager.getManager(project)
            .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
            .let { PsiUtils.getValidResults(it) }
    }

    companion object {
        @JvmStatic
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, FunctionTSAttributeReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FunctionTSAttributeReference> { ref ->
            val metaService = TSMetaModelAccess.getInstance(ref.project)
            val featureName = ref.element.text.trim()
            val typeName = findItemTypeReference(ref.element)
            val metaItem = metaService.findMetaItemByName(typeName)
            val result: Array<ResolveResult> = if (metaItem == null) {
                metaService.findMetaEnumByName(typeName)
                    ?.let { arrayOf(EnumResolveResult(it)) }
                    ?: ResolveResult.EMPTY_ARRAY
            } else {
                val itemService = TSMetaItemService.getInstance(ref.project)
                val attributes = itemService
                    .findAttributesByName(metaItem, featureName, true)
                    .map { AttributeResolveResult(it) }

                val relations = itemService
                    .findRelationEndsByQualifier(metaItem, featureName, true)
                    .map { RelationEndResolveResult(it) }

                (attributes + relations).toTypedArray()
            }

            // no need to track with PsiModificationTracker.MODIFICATION_COUNT due manual cache reset via custom Mixin
            CachedValueProvider.Result.create(
                result,
                metaService.getMetaModel()
            )
        }

        private fun findItemTypeReference(element: PsiElement): String {
            val parent = element.parent.parent
            val parameterName = PsiTreeUtil.findChildOfType(parent, ImpexAnyHeaderParameterName::class.java)
            if (parameterName != null) {
                val references = parameterName.references
                if (references.isNotEmpty()) {
                    val reference = references.first().resolve()
                    return obtainTypeName(reference)
                }
            }
            return ""
        }

        private fun obtainTypeName(reference: PsiElement?): String {
            val typeTag = PsiTreeUtil.findFirstParent(reference) { value -> value is XmlTag }
            return if (typeTag != null) (typeTag as XmlTag).attributes.first { it.name == "type" }.value!! else ""
        }
    }

}
