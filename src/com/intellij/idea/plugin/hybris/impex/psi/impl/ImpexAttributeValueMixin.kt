/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier.*
import com.intellij.idea.plugin.hybris.impex.constants.modifier.TypeModifier
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAttribute
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexJavaClassBaseReference
import com.intellij.idea.plugin.hybris.psi.reference.LanguageReference
import com.intellij.idea.plugin.hybris.system.type.psi.reference.SpringReference
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import java.io.Serial
import javax.lang.model.SourceVersion

abstract class ImpexAttributeValueMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpexAnyAttributeValue {

    private val disableInterceptorBeansExclusionRegex = "[,'\"]".toRegex()
    private var myReference: PsiReference? = null

    override fun getReferences(): Array<PsiReference> = myReference
        ?.let { arrayOf(it) }
        ?: computeReference()

    private fun computeReference() = if (getParentOfType<ImpexFullHeaderTypeImpl>(false) != null) {
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
            ?.let { TypeModifier.getByModifierName(it) }
            ?: return null

        return when (modifierName) {
            TypeModifier.PROCESSOR -> if (SourceVersion.isName(text)) {
                ImpexJavaClassBaseReference(this)
            } else null

            // TODO: multi values and wrapped strings are not yet supported.
            // see https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/9ce1b60e12714a7dba6ea7e66b4f7acd.html?locale=en-US#disable-interceptors-via-impex
            TypeModifier.DISABLE_INTERCEPTOR_BEANS -> {
                if (node.text.contains(disableInterceptorBeansExclusionRegex)) return null
                else SpringReference(this, node.text)
            }

            else -> null
        }
    }

    private fun computeAttributeModifierReference(): PsiReference? {
        val modifierName = (parent as? ImpexAttribute)
            ?.anyAttributeName
            ?.text
            ?.let { AttributeModifier.getByModifierName(it) }
            ?: return null

        return when (modifierName) {
            TRANSLATOR,
            CELL_DECORATOR -> if (SourceVersion.isName(text)) {
                ImpexJavaClassBaseReference(this)
            } else null

            LANG -> LanguageReference(this)
            else -> null
        }
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
