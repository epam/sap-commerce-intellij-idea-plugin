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
import com.intellij.idea.plugin.hybris.psi.references.TypeSystemReferenceBase
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService
import com.intellij.idea.plugin.hybris.type.system.model.Attribute
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement
import java.util.*

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class FunctionTypeSystemAttributeReference(owner: ImpexParameter) : TypeSystemReferenceBase<ImpexParameter>(owner) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val metaService = TSMetaModelService.getInstance(project)
        val featureName = element.text.trim()
        val typeName = findItemTypeReference()
        val metaItem = metaService.findMetaItemByName(typeName)

        if (metaItem == null) {
            // TODO: why call this method seconds time?
            val metaEnum = metaService.findMetaEnumByName(findItemTypeReference())
            if (metaEnum != null) {
                val result = metaEnum.retrieveDom()
                return arrayOf(EnumResolveResult(result))
            }
        } else {
            val result = metaItem
                    .findAttributesByName(featureName, true)
                    .map { it.retrieveDom() }
                    .filter { Objects.nonNull(it) }
                    .map { AttributeResolveResult(it!!) }

            metaItem.findReferenceEndsByRole(featureName, true)
                    .map { it.retrieveDom() }
                    .filter { Objects.nonNull(it) }
                    .map { RelationElementResolveResult(it!!) }

            return result.toTypedArray()
        }

        return ResolveResult.EMPTY_ARRAY
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
        val typeTag = PsiTreeUtil.findFirstParent(reference, { value -> value is XmlTag })
        return if (typeTag != null) (typeTag as XmlTag).attributes.first { it.name == "type" }.value!! else ""
    }
    
    private class AttributeResolveResult(private val myDomAttribute: Attribute) : TypeSystemReferenceBase.TypeSystemResolveResult {

        override fun getElement(): PsiElement? {
            return myDomAttribute.qualifier.xmlAttributeValue
        }

        override fun isValidResult(): Boolean {
            return element != null
        }

        override fun getSemanticDomElement(): DomElement {
            return myDomAttribute
        }
    }

    private class RelationElementResolveResult(private val myDomRelationEnd: RelationElement) : TypeSystemReferenceBase.TypeSystemResolveResult {

        override fun getElement(): PsiElement? {
            return myDomRelationEnd.qualifier.xmlAttributeValue
        }

        override fun isValidResult(): Boolean {
            return element != null
        }

        override fun getSemanticDomElement(): DomElement {
            return myDomRelationEnd
        }
    }

}
