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
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpExParameter
import sap.commerce.toolset.impex.psi.references.ImpExFunctionTSAttributeReference
import sap.commerce.toolset.impex.psi.references.ImpExFunctionTSItemReference
import sap.commerce.toolset.impex.psi.references.ImpExHeaderAbbreviationReference
import sap.commerce.toolset.typeSystem.meta.TSModificationTracker
import java.io.Serial

abstract class ImpExParameterMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), ImpExParameter {

    override fun getReference() = references.firstOrNull()

    override fun getReferences(): Array<PsiReference> = CachedValuesManager.getManager(project).getCachedValue(this) {
        val references = if (childrenOfType<ImpExDocumentIdUsage>().isNotEmpty()) emptyArray()
        else buildList<PsiReference> {
            if (inlineTypeName != null) {
                add(ImpExFunctionTSItemReference(this@ImpExParameterMixin))

                // attribute can be a Macro item(CMSLinkComponent.$contentCV)
                if (childrenOfType<ImpExMacroUsageDec>().isEmpty()) {
                    add(getSuitableReference())
                }
            } else {
                add(getSuitableReference())
            }
        }
            .toTypedArray()

        CachedValueProvider.Result.create(
            references,
            TSModificationTracker.getInstance(project), PsiModificationTracker.MODIFICATION_COUNT
        )
    }

    override fun clone(): Any {
        val result = super.clone() as ImpExParameterMixin
        return result
    }

    override fun subtreeChanged() {
        removeUserData(ImpExFunctionTSItemReference.CACHE_KEY)
        removeUserData(ImpExFunctionTSAttributeReference.CACHE_KEY)
        removeUserData(ImpExHeaderAbbreviationReference.CACHE_KEY)
    }

    private fun getSuitableReference() = if (isHeaderAbbreviation()) ImpExHeaderAbbreviationReference(this)
    else ImpExFunctionTSAttributeReference(this)

    companion object {
        @Serial
        private val serialVersionUID: Long = -8834268360363491069L
    }
}
