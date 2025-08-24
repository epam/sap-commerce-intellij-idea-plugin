/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.childrenOfType
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpExParameter
import sap.commerce.toolset.impex.psi.references.ImpExFunctionTSAttributeReference
import sap.commerce.toolset.impex.psi.references.ImpExFunctionTSItemReference
import java.io.Serial

abstract class ImpExParameterMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpExParameter {

    private val myReferences = mutableListOf<PsiReferenceBase<out PsiElement>>()
    private var previousText: String? = null

    override fun getReference() = references.firstOrNull()

    override fun getReferences(): Array<PsiReference> {
        if (previousText != text) {
            myReferences.clear()
        }

        if (myReferences.isEmpty() || previousText == null) {
            if (inlineTypeName != null) {
                myReferences.add(ImpExFunctionTSItemReference(this))

                if (childrenOfType<ImpExMacroUsageDec>().isEmpty()) {
                    // attribute can be a Macro item(CMSLinkComponent.$contentCV)
                    myReferences.add(ImpExFunctionTSAttributeReference(this))
                }
            } else {
                myReferences.add(ImpExFunctionTSAttributeReference(this))
            }
        }

        return myReferences.toTypedArray()
    }

    override fun clone(): Any {
        val result = super.clone() as ImpExParameterMixin
        result.previousText = null
        result.myReferences.clear()
        return result
    }

    override fun subtreeChanged() {
        removeUserData(ImpExFunctionTSItemReference.CACHE_KEY)
        removeUserData(ImpExFunctionTSAttributeReference.CACHE_KEY)
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -8834268360363491069L
    }
}
