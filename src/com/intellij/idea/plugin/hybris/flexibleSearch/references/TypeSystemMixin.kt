package com.intellij.idea.plugin.hybris.flexibleSearch.references

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableName
import com.intellij.idea.plugin.hybris.psi.references.TypeSystemReferenceBase
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService
import com.intellij.idea.plugin.hybris.type.system.model.ItemType
import com.intellij.idea.plugin.hybris.type.system.model.Relation
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */

abstract class TypeNameMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), FlexibleSearchTableName {

    private var myReference: TypeSystemItemRef? = null

    override fun getReferences(): Array<PsiReference> {
        if (myReference == null) {
            myReference = TypeSystemItemRef(this)
        }
        return arrayOf(myReference!!)
    }

    override fun clone(): Any {
        val result = super.clone() as TypeNameMixin
        result.myReference = null
        return result
    }
}

class TypeSystemItemRef(owner: FlexibleSearchTableName) : TypeSystemReferenceBase<FlexibleSearchTableName>(owner) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val lookingForName = element.text.replace("!", "")
        val res0 = Optional.ofNullable(TSMetaModelService.getInstance(project).findMetaItemByName(lookingForName))
                .map { it.retrieveAllDomsStream() }
                .orElse(Stream.empty())
                .map { ItemTypeResolveResult(it) }
                .collect(Collectors.toList())


        val res1 = TSMetaModelService.getInstance(project).findRelationByName(lookingForName)
                .distinctBy { it.name }
                .map { it.retrieveDom() }
                .map { RelationResolveResult(it) }
                .toList()

        return (res0 + res1).toTypedArray()
    }

    private class ItemTypeResolveResult(private val myDomItemType: ItemType) : ResolveResult {

        override fun getElement(): PsiElement? {
            val codeAttr = myDomItemType.code
            return codeAttr.xmlAttributeValue
        }

        override fun isValidResult() = element != null
    }

    private class RelationResolveResult(private val myDomItemType: Relation) : ResolveResult {

        override fun getElement(): PsiElement? {
            val codeAttr = myDomItemType.code
            return codeAttr.xmlAttributeValue
        }

        override fun isValidResult() = element != null
    }

}