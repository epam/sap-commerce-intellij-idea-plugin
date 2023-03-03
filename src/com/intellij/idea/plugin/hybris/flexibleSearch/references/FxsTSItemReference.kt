package com.intellij.idea.plugin.hybris.flexibleSearch.references

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableName
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.ItemResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationResolveResult
import com.intellij.openapi.util.Key
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*

class FxsTSItemReference(owner: FlexibleSearchTableName) : TSReferenceBase<FlexibleSearchTableName>(owner), HighlightedReference {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { PsiUtils.getValidResults(it) }

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, FxsTSItemReference>>("HYBRIS_TS_CACHED_REFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, FxsTSItemReference> { ref ->
            val lookingForName = ref.element.text.replace("!", "")
            val modelAccess = TSMetaModelAccess.getInstance(ref.project)

            val items = (modelAccess.findMetaItemByName(lookingForName)
                ?.declarations
                ?.map { ItemResolveResult(it) }
                ?: emptyList())

            val relations = modelAccess.findRelationByName(lookingForName)
                .distinctBy { it.name }
                .map { RelationResolveResult(it) }

            CachedValueProvider.Result.create(
                (items + relations).toTypedArray(),
                modelAccess.getMetaModel(), PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

}