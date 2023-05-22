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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexValueLine
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSAttributeReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSItemReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSSubTypeItemReference
import com.intellij.idea.plugin.hybris.psi.impl.ASTWrapperReferencePsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.siblings
import java.io.Serial

abstract class ImpexHeaderTypeNameMixin(astNode: ASTNode) : ASTWrapperReferencePsiElement(astNode), ImpexHeaderTypeName {

    override fun createReference() = ImpexTSItemReference(this)

    override fun subtreeChanged() {
        putUserData(ImpexTSItemReference.CACHE_KEY, null)

        val headerLine = PsiTreeUtil.getParentOfType(this, ImpexHeaderLine::class.java) ?: return

        // reset cache for header parameters
        headerLine
            .fullHeaderParameterList
            .map { it.anyHeaderParameterName }
            .forEach { it.putUserData(ImpexTSAttributeReference.CACHE_KEY, null) }

        // reset cache for sub types
        val subTypesIterator = headerLine.siblings(withSelf = false).iterator()
        var proceed = true

        while (proceed && subTypesIterator.hasNext()) {
            when (val psi = subTypesIterator.next()) {
                is ImpexHeaderLine -> proceed = false
                is ImpexValueLine -> psi.subTypeName
                    ?.putUserData(ImpexTSSubTypeItemReference.CACHE_KEY, null)
            }
        }
    }

    companion object {
        @Serial
        private val serialVersionUID = -4201751443049498642L
    }
}
