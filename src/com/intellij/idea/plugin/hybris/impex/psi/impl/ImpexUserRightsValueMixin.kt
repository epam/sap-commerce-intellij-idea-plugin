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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.ImpexUserRightsValue
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSItemReference
import com.intellij.idea.plugin.hybris.psi.impl.ASTWrapperReferencePsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.elementType
import java.io.Serial

abstract class ImpexUserRightsValueMixin(astNode: ASTNode) : ASTWrapperReferencePsiElement(astNode), ImpexUserRightsValue {

    override fun createReference(): PsiReferenceBase<out PsiElement>? {
        val headerParameter = this.headerParameter ?: return null

        return when (headerParameter.firstChild.elementType) {
            ImpexTypes.TYPE -> ImpexTSItemReference(this)
//            ImpexTypes.TARGET -> with(PsiTreeUtilExt.getLeafsOfElementType(it, ImpexTypes.FIELD_VALUE)) {
//                getOrNull(0)?.let { highlight(ImpexTypes.HEADER_TYPE, holder, it) }
//                getOrNull(1)?.let { highlight(ImpexTypes.HEADER_PARAMETER_NAME, holder, it) }
//            }
            else -> null
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1657911985932608306L
    }

}
