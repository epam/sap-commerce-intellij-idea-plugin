package com.intellij.idea.plugin.hybris.flexibleSearch.references

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.*
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.idea.plugin.hybris.system.type.model.RelationElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.util.*

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
abstract class ColumnReferenceMixin(node: ASTNode) : ASTWrapperPsiElement(node), FlexibleSearchColumnReference {

    private var reference: TSAttributeReference? = null

    override fun getReferences(): Array<PsiReference?> {
        if (reference == null) {
            reference = TSAttributeReference(this)
        }
        return arrayOf(reference)
    }

    override fun clone(): Any {
        val result = super.clone() as ColumnReferenceMixin
        result.reference = null
        return result
    }

}

internal class TSAttributeReference(owner: FlexibleSearchColumnReference) : TSReferenceBase<FlexibleSearchColumnReference>(owner), HighlightedReference {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val featureName = element.text.replace("!", "")
        if (hasPrefix(element)) {
            return findReference(deepSearchOfTypeReference(element, element.firstChild.text), element.lastChild.text)
        }
        return findReference(findItemTypeReference(), featureName)
    }

    private fun hasPrefix(element: FlexibleSearchColumnReference) = ((element.firstChild as LeafPsiElement).elementType == FlexibleSearchTypes.TABLE_NAME_IDENTIFIER)

    private fun findReference(itemType: Optional<FlexibleSearchTableName>, refName: String): Array<ResolveResult> {
        val metaService = TSMetaModelAccess.getInstance(project)
        val metaItem = itemType
                .map { it.text.replace("!", "") }
                .map { metaService.findMetaItemByName(it) }

        if (!metaItem.isPresent) {
            return ResolveResult.EMPTY_ARRAY
        }

        val attributes = metaItem.get().allAttributes
            .filter { refName.equals(it.name, true) }
            .mapNotNull { it.retrieveDom() }
            .map { AttributeResolveResult(it) }

        val relations = metaItem.get().allRelationEnds
            .filter { refName.equals(it.name, true) }
            .mapNotNull { it.retrieveDom() }
            .map { RelationElementResolveResult(it) }

        return (attributes + relations).toTypedArray() 
    }

    private fun findItemTypeReference(): Optional<FlexibleSearchTableName> {
        return Optional.ofNullable(PsiTreeUtil.getParentOfType(element, FlexibleSearchQuerySpecification::class.java))
                .map { PsiTreeUtil.findChildOfType(it, FlexibleSearchFromClause::class.java) }
                .map { it!!.tableReferenceList }
                .map { PsiTreeUtil.findChildOfType(it, FlexibleSearchTableName::class.java) }
    }

    private fun deepSearchOfTypeReference(elem: PsiElement, prefix: String): Optional<FlexibleSearchTableName> {
        val parent = PsiTreeUtil.getParentOfType(elem, FlexibleSearchQuerySpecification::class.java)
        val tables = PsiTreeUtil.findChildrenOfType(parent, FlexibleSearchTableReference::class.java).toList()

        val tableReference = tables.find {
            val tableName = PsiTreeUtil.findChildOfAnyType(it, FlexibleSearchTableName::class.java)
            val corName = findCorName(tableName)
            prefix == corName
        }
        return if (tableReference == null && parent != null) {
            deepSearchOfTypeReference(parent, prefix)
        } else {
            Optional.ofNullable(PsiTreeUtil.findChildOfType(tableReference, FlexibleSearchTableName::class.java))
        }
    }

    private fun findCorName(tableName: FlexibleSearchTableName?) : String {
        val corNameEl = PsiTreeUtil.findSiblingForward(tableName!!.originalElement, FlexibleSearchTypes.CORRELATION_NAME, null)
        if (corNameEl == null) {
            return tableName.text
        } 
        return corNameEl.text
    }
        

    private class AttributeResolveResult(private val myDomAttribute: Attribute) : TSResolveResult {
        override fun getElement() = myDomAttribute.qualifier.xmlAttributeValue
        override fun isValidResult() = element != null
        override fun getSemanticDomElement() = myDomAttribute
    }

    private class RelationElementResolveResult(private val myDomRelationEnd: RelationElement) : TSResolveResult {
        override fun getElement() = myDomRelationEnd.qualifier.xmlAttributeValue
        override fun isValidResult() = element != null
        override fun getSemanticDomElement() = myDomRelationEnd
    }

}
