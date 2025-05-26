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

package com.intellij.idea.plugin.hybris.impex.psi.impl


import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexFunctionTSAttributeReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexFunctionTSItemReference
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.childrenOfType
import java.io.Serial

abstract class ImpexParameterMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpexParameter {

    private val myReferences = mutableListOf<PsiReferenceBase<out PsiElement>>()
    private var previousText: String? = null

    override fun getReference() = references.firstOrNull()

    override fun getReferences(): Array<PsiReference> {
        if (previousText != text) {
            myReferences.clear()
        }

        if (myReferences.isEmpty() || previousText == null) {
            if (inlineTypeName != null) {
                myReferences.add(ImpexFunctionTSItemReference(this))

                if (childrenOfType<ImpexMacroUsageDec>().isEmpty()) {
                    // attribute can be a Macro item(CMSLinkComponent.$contentCV)
                    myReferences.add(ImpexFunctionTSAttributeReference(this))
                }
            } else {
                myReferences.add(ImpexFunctionTSAttributeReference(this))
            }
        }

        return myReferences.toTypedArray()
    }

    override fun clone(): Any {
        val result = super.clone() as ImpexParameterMixin
        result.previousText = null
        result.myReferences.clear()
        return result
    }

    override fun subtreeChanged() {
        removeUserData(ImpexFunctionTSItemReference.CACHE_KEY)
        removeUserData(ImpexFunctionTSAttributeReference.CACHE_KEY)
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -8834268360363491069L
    }
}
