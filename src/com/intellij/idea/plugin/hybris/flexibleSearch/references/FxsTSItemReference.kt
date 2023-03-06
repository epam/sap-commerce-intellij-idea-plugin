package com.intellij.idea.plugin.hybris.flexibleSearch.references

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableName
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.ItemResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationResolveResult
import com.intellij.openapi.util.Key
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class FxsTSItemReference(owner: FlexibleSearchTableName) : TSReferenceBase<FlexibleSearchTableName>(owner) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxsTSItemReference>>("HYBRIS_TS_CACHED_REFERENCE")

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