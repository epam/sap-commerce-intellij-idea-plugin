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

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.impex.psi.ImpExHeaderTypeName
import sap.commerce.toolset.impex.psi.references.ImpExTSAttributeReference
import sap.commerce.toolset.impex.psi.references.ImpExTSItemReference
import sap.commerce.toolset.impex.psi.references.ImpExTSSubTypeItemReference
import sap.commerce.toolset.psi.impl.ASTWrapperReferencePsiElement
import java.io.Serial

abstract class ImpExHeaderTypeNameMixin(astNode: ASTNode) : ASTWrapperReferencePsiElement(astNode), ImpExHeaderTypeName {

    override fun createReference() = ImpExTSItemReference(this)

    override fun subtreeChanged() {
        removeUserData(ImpExTSItemReference.CACHE_KEY)

        val headerLine = PsiTreeUtil.getParentOfType(this, ImpExHeaderLine::class.java) ?: return

        // reset cache for header parameters
        headerLine
            .fullHeaderParameterList
            .map { it.anyHeaderParameterName }
            .forEach { it.removeUserData(ImpExTSAttributeReference.CACHE_KEY) }

        // reset cache for subtypes
        headerLine.valueLines
            .mapNotNull { it.subTypeName }
            .forEach { it.removeUserData(ImpExTSSubTypeItemReference.CACHE_KEY) }
    }

    companion object {
        @Serial
        private val serialVersionUID = -4201751443049498642L
    }
}
