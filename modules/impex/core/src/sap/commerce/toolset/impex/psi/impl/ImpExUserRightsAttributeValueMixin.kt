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
import com.intellij.psi.util.elementType
import com.intellij.psi.util.firstLeaf
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.psi.ImpExUserRightsAttributeValue
import sap.commerce.toolset.impex.psi.references.ImpExUserRightsTSAttributeReference
import sap.commerce.toolset.psi.impl.ASTWrapperReferencePsiElement
import java.io.Serial

abstract class ImpExUserRightsAttributeValueMixin(astNode: ASTNode) : ASTWrapperReferencePsiElement(astNode), ImpExUserRightsAttributeValue {

    override fun createReference() = if (headerParameter?.firstLeaf()?.elementType == ImpExTypes.TARGET) ImpExUserRightsTSAttributeReference(this)
    else null

    override fun subtreeChanged() {
        removeUserData(ImpExUserRightsTSAttributeReference.CACHE_KEY)
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -7547018820067281724L
    }
}