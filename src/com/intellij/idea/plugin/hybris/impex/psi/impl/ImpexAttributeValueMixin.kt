/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier.*
import com.intellij.idea.plugin.hybris.impex.constants.modifier.TypeModifier
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpExJavaEnumValueReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexJavaClassReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSItemReference
import com.intellij.idea.plugin.hybris.psi.reference.LanguageReference
import com.intellij.idea.plugin.hybris.system.type.psi.reference.SpringReference
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiReference
import com.intellij.psi.util.parentOfType
import java.io.Serial
import javax.lang.model.SourceVersion

abstract class ImpexAttributeValueMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpexAnyAttributeValue {

    // TODO: multi values and wrapped strings are not yet supported.
    // see https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/9ce1b60e12714a7dba6ea7e66b4f7acd.html?locale=en-US#disable-interceptors-via-impex
    private val disableInterceptorExclusionRegex = "[,'\"]".toRegex()

    private var myReference: PsiReference? = null

    override fun getReferences(): Array<PsiReference> = myReference
        ?.takeIf { it.rangeInElement.length == node.textLength }
        ?.let { arrayOf(it) }
        ?: computeReference()

    override fun getReference() = references.firstOrNull()

    private fun computeReference() = if (parentOfType<ImpexFullHeaderTypeImpl>(false) != null) {
        computeTypeModifierReference()
    } else {
        computeAttributeModifierReference()
    }
        ?.also { myReference = it }
        ?.let { arrayOf(it) }
        ?: PsiReference.EMPTY_ARRAY

    private fun computeTypeModifierReference(): PsiReference? {
        val modifierName = (parent as? ImpexAttribute)
            ?.anyAttributeName
            ?.text
            ?.let { TypeModifier.getModifier(it) }
            ?: return null

        return when (modifierName) {
            TypeModifier.PROCESSOR -> if (SourceVersion.isName(text)) {
                ImpexJavaClassReference(this)
            } else null

            TypeModifier.DISABLE_INTERCEPTOR_TYPES -> if (node.text.contains(disableInterceptorExclusionRegex)) return null
            else ImpExJavaEnumValueReference(this, HybrisConstants.CLASS_FQN_INTERCEPTOR_TYPE)

            TypeModifier.DISABLE_INTERCEPTOR_BEANS -> if (node.text.contains(disableInterceptorExclusionRegex)) return null
            else SpringReference(this, node.text)

            TypeModifier.DISABLE_UNIQUE_ATTRIBUTES_VALIDATOR_FOR_TYPES -> if (node.text.contains(disableInterceptorExclusionRegex)) return null
            else ImpexTSItemReference(this)

            else -> null
        }
    }

    private fun computeAttributeModifierReference(): PsiReference? {
        val modifierName = (parent as? ImpexAttribute)
            ?.anyAttributeName
            ?.text
            ?.let { AttributeModifier.getModifier(it) }
            ?: return null

        return when (modifierName) {
            TRANSLATOR,
            CELL_DECORATOR -> if (SourceVersion.isName(text)) {
                ImpexJavaClassReference(this)
            } else null

            LANG -> LanguageReference(this)
            else -> null
        }
    }

    override fun subtreeChanged() {
        removeUserData(ImpexTSItemReference.CACHE_KEY)

        myReference = null
    }

    override fun clone(): Any {
        val result = super.clone() as ImpexAttributeValueMixin
        result.myReference = null
        return result
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -1264040766293615937L
    }
}
