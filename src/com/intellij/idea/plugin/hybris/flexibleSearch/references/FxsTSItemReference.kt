package com.intellij.idea.plugin.hybris.flexibleSearch.references

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableName
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.ItemResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationResolveResult
import com.intellij.psi.ResolveResult

class FxsTSItemReference(owner: FlexibleSearchTableName) : TSReferenceBase<FlexibleSearchTableName>(owner), HighlightedReference {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val lookingForName = element.text.replace("!", "")

        val items = (TSMetaModelAccess.getInstance(project).findMetaItemByName(lookingForName)
            ?.declarations
            ?.map { ItemResolveResult(it) }
            ?: emptyList())

        val relations = TSMetaModelAccess.getInstance(project).findRelationByName(lookingForName)
                .distinctBy { it.name }
                .map { RelationResolveResult(it) }

        return PsiUtils.getValidResults((items + relations).toTypedArray())
    }

}