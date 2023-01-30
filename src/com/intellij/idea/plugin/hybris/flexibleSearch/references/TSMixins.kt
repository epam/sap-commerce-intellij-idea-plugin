package com.intellij.idea.plugin.hybris.flexibleSearch.references

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableName
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.ItemResolveResult
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.RelationResolveResult
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */

abstract class TypeNameMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode),
    FlexibleSearchTableName {

    private var myReference: TSItemRef? = null

    override fun getReferences(): Array<PsiReference> {
        if (PsiUtils.shouldCreateNewReference(myReference, text)) {
            myReference = TSItemRef(this)
        }
        return arrayOf(myReference!!)
    }

    override fun clone(): Any {
        val result = super.clone() as TypeNameMixin
        result.myReference = null
        return result
    }

    companion object {
        private const val serialVersionUID: Long = -1523585420205611226L
    }
}

class TSItemRef(owner: FlexibleSearchTableName) : TSReferenceBase<FlexibleSearchTableName>(owner), HighlightedReference {

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