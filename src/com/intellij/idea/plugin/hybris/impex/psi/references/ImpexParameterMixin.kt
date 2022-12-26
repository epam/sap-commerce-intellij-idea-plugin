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


import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.impex.psi.ImpexParameter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.IElementType
import java.util.Optional

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
abstract class ImpexParameterMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpexParameter {

    private var myReference: PsiReference? = null

    override fun getReferences(): Array<PsiReference> {
        val leafType = Optional.ofNullable(firstChild)
                .map<ASTNode> { it.node }
                .map<IElementType> { it.elementType }
                .orElse(null)

        if (ImpexTypes.DOCUMENT_ID == leafType) {
            if (myReference == null) {
                myReference = ImpexDocumentIdReference(this)
            }
            return arrayOf(myReference!!)
        }

        if (myReference == null) {
            myReference = FunctionTSAttributeReference(this)
        }
        return arrayOf(myReference!!)
    }

    override fun clone(): Any {
        val result = super.clone() as ImpexParameterMixin
        result.myReference = null
        return result
    }
}
