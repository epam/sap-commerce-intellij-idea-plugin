/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiReference
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier
import sap.commerce.toolset.impex.constants.modifier.AttributeModifier.*
import sap.commerce.toolset.impex.constants.modifier.InterceptorProvider
import sap.commerce.toolset.impex.constants.modifier.TypeModifier
import sap.commerce.toolset.impex.psi.ImpExAnyAttributeValue
import sap.commerce.toolset.impex.psi.ImpExAttribute
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.psi.references.ImpExJavaClassReference
import sap.commerce.toolset.impex.psi.references.ImpExJavaEnumValueReference
import sap.commerce.toolset.impex.psi.references.ImpExTSItemReference
import sap.commerce.toolset.project.psi.reference.LanguageReference
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import java.io.Serial
import javax.lang.model.SourceVersion

abstract class ImpExAttributeValueMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpExAnyAttributeValue {

    // TODO: multi values and wrapped strings are not yet supported.
    // see https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/9ce1b60e12714a7dba6ea7e66b4f7acd.html?locale=en-US#disable-interceptors-via-impex
    private val disableInterceptorExclusionRegex = "[,'\"]".toRegex()

    override fun getReference() = references.firstOrNull()
    override fun getReferences(): Array<PsiReference> = CachedValuesManager.getManager(project).getCachedValue(this) {
        CachedValueProvider.Result.create(
            collectReferences(),
            TSModificationTracker.getInstance(project), this
        )
    }

    private fun collectReferences() = if (parentOfType<ImpExFullHeaderTypeImpl>(false) != null) {
        computeTypeModifierReferences()
    } else {
        computeAttributeModifierReferences()
    }

    private fun computeTypeModifierReferences(): Array<PsiReference> {
        val modifierName = (parent as? ImpExAttribute)
            ?.anyAttributeName
            ?.text
            ?.let { TypeModifier.getModifier(it) }
            ?: return emptyArray()

        return when (modifierName) {
            TypeModifier.PROCESSOR -> if (SourceVersion.isName(text)) {
                ImpExJavaClassReference(this)
            } else null

            TypeModifier.DISABLE_INTERCEPTOR_TYPES -> if (node.text.contains(disableInterceptorExclusionRegex)) null
            else ImpExJavaEnumValueReference(this, HybrisConstants.CLASS_FQN_INTERCEPTOR_TYPE)

            TypeModifier.DISABLE_INTERCEPTOR_BEANS -> if (node.text.contains(disableInterceptorExclusionRegex)) null
            else InterceptorProvider.EP.extensionList
                .map { it.reference(this, node.text) }
                .firstOrNull()

            TypeModifier.DISABLE_UNIQUE_ATTRIBUTES_VALIDATOR_FOR_TYPES -> if (node.text.contains(disableInterceptorExclusionRegex)) null
            else ImpExTSItemReference(this)

            else -> null
        }
            ?.let { arrayOf(it) }
            ?: emptyArray()
    }

    private fun computeAttributeModifierReferences(): Array<PsiReference> {
        val modifierName = (parent as? ImpExAttribute)
            ?.anyAttributeName
            ?.text
            ?.let { AttributeModifier.getModifier(it) }
            ?: return emptyArray()

        return when (modifierName) {
            TRANSLATOR,
            CELL_DECORATOR -> if (SourceVersion.isName(text)) {
                arrayOf(ImpExJavaClassReference(this))
            } else null

            LANG -> arrayOf(LanguageReference(this))
            DEFAULT -> this.parentOfType<ImpExFullHeaderParameter>()
                ?.takeUnless { this.text.contains(ImpExConstants.INVERTED_COMMA) }
                ?.let {
                    val tsContext = it.typeSystemContext ?: return@let null
                    it.collectDocIdReferences(this, tsContext)
                        ?: it.collectTSReferences(this, tsContext) { arrayOf(this) }
                }

            else -> null
        }
            ?: emptyArray()
    }

    override fun subtreeChanged() {
        removeUserData(ImpExTSItemReference.CACHE_KEY)
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -1264040766293615937L
    }
}
