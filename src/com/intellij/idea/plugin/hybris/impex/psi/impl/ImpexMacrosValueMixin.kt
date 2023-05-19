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
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroValue
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexPropertiesBaseReference
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil.prevLeaf

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
abstract class ImpexMacrosValueMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpexMacroValue {

    private var myReference: PsiReference? = null

    override fun getReferences(): Array<PsiReference> {
        val prevLeaf = prevLeaf(this)
        if (prevLeaf != null && prevLeaf.text.contains(HybrisConstants.IMPEX_CONFIG_PREFIX)) {
            if (myReference == null) {
                myReference = ImpexPropertiesBaseReference(prevLeaf, this)
            }
            return arrayOf(myReference!!)
        }
        if (this.text.contains(HybrisConstants.IMPEX_CONFIG_PREFIX)) {
            if (myReference == null) {
                myReference = ImpexPropertiesBaseReference(null, this)
            }
            return arrayOf(myReference!!)
        }

        return PsiReference.EMPTY_ARRAY
    }

    override fun clone(): Any {
        val result = super.clone() as ImpexMacrosValueMixin
        result.myReference = null
        return result
    }
}
