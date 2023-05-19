/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.references.FunctionTSAttributeReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexDocumentIdReference
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
abstract class ImpexParameterMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpexParameter {

    private var myReference: PsiReferenceBase.Poly<out PsiElement>? = null

    override fun getReference() = references.firstOrNull()

    override fun getReferences(): Array<PsiReference> {
        val leafType = firstChild
            ?.node
            ?.elementType

        if (ImpexTypes.DOCUMENT_ID == leafType) {
            if (myReference == null) {
                myReference = ImpexDocumentIdReference(this)
            }
            return arrayOf(myReference!!)
        }

        if (PsiUtils.shouldCreateNewReference(myReference, text)) {
            myReference = FunctionTSAttributeReference(this)
        }
        return arrayOf(myReference!!)
    }

    override fun clone(): Any {
        val result = super.clone() as ImpexParameterMixin
        result.myReference = null
        return result
    }

    override fun subtreeChanged() {
        putUserData(FunctionTSAttributeReference.CACHE_KEY, null)
    }

    companion object {
        private const val serialVersionUID: Long = -8834268360363491069L
    }
}
