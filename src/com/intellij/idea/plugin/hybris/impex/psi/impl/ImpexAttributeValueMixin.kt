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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyAttributeValue
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexJavaClassBaseReference
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import javax.lang.model.SourceVersion.isName

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
abstract class ImpexAttributeValueMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpexAnyAttributeValue {

    private var myReference: ImpexJavaClassBaseReference? = null

    override fun getReferences(): Array<PsiReference> {

        if (isName(text)) {
            if (myReference == null) {
                myReference = ImpexJavaClassBaseReference(this)
            }
            return arrayOf(myReference!!)
        }

        return PsiReference.EMPTY_ARRAY
    }

    override fun clone(): Any {
        val result = super.clone() as ImpexAttributeValueMixin
        result.myReference = null
        return result
    }
}
