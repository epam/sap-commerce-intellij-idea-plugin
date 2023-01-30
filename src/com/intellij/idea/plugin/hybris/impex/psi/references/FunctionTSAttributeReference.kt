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
import com.intellij.idea.plugin.hybris.impex.psi.references.result.EnumResolveResult
import com.intellij.idea.plugin.hybris.psi.reference.TSReferenceBase
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.idea.plugin.hybris.system.type.model.RelationElement
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement
import java.util.*

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class FunctionTSAttributeReference(owner: ImpexParameter) : TSReferenceBase<ImpexParameter>(owner) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val indicator = ProgressManager.getInstance().progressIndicator
        if (indicator != null && indicator.isCanceled) return ResolveResult.EMPTY_ARRAY

        var result = element.getUserData(ImpexParameterMixin.CACHE_KEY)

        if (result != null) return result

        val metaService = TSMetaModelAccess.getInstance(project)
        val featureName = element.text.trim()
        val typeName = findItemTypeReference()
        val metaItem = metaService.findMetaItemByName(typeName)

        if (metaItem == null) {
            result = metaService.findMetaEnumByName(typeName)
                ?.retrieveDom()
                ?.let { arrayOf(EnumResolveResult(it)) }
                ?: ResolveResult.EMPTY_ARRAY
        } else {
            val attributes = TSMetaItemService.getInstance(project)
                    .findAttributesByName(metaItem, featureName, true)
                    .mapNotNull { it.retrieveDom() }
                    .map { AttributeResolveResult(it) }

            val relations = TSMetaItemService.getInstance(project).findRelationEndsByQualifier(metaItem, featureName, true)
                    .mapNotNull { it.retrieveDom() }
                    .map { RelationElementResolveResult(it) }

            result = (attributes + relations).toTypedArray()
        }

        element.putUserData(ImpexParameterMixin.CACHE_KEY, result)
        return result!!;
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        if (resolveResults.size != 1) return null

        return with (resolveResults[0]) {
            if (this.isValidResult) return@with this.element
            return@with null
        }
    }

    private fun findItemTypeReference(): String {
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
    
    private class AttributeResolveResult(private val myDomAttribute: Attribute) :
        TSResolveResult {

        override fun getElement() = myDomAttribute.qualifier.xmlAttributeValue
        override fun isValidResult() = element != null && myDomAttribute.isValid
        override fun getSemanticDomElement() = myDomAttribute
    }

    private class RelationElementResolveResult(private val myDomRelationEnd: RelationElement) :
        TSResolveResult {

        override fun getElement() = myDomRelationEnd.qualifier.xmlAttributeValue
        override fun isValidResult() = element != null && myDomRelationEnd.isValid
        override fun getSemanticDomElement() = myDomRelationEnd
    }

}
