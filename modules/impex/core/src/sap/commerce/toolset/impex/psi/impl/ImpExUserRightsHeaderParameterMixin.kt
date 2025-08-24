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
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.impex.psi.ImpExUserRights
import sap.commerce.toolset.impex.psi.ImpExUserRightsHeaderLine
import sap.commerce.toolset.impex.psi.ImpExUserRightsHeaderParameter
import sap.commerce.toolset.impex.psi.ImpExUserRightsValueGroup
import java.io.Serial

abstract class ImpExUserRightsHeaderParameterMixin(node: ASTNode) : ASTWrapperPsiElement(node), ImpExUserRightsHeaderParameter {

    override fun getHeaderLine(): ImpExUserRightsHeaderLine? = parentOfType<ImpExUserRightsHeaderLine>()
    override fun getColumnNumber(): Int? = getHeaderLine()
        ?.userRightsHeaderParameterList
        ?.indexOf(this)
        ?.takeIf { it != -1 }

    override fun getValueGroups(): Collection<ImpExUserRightsValueGroup> {
        val columnNumber = this.columnNumber ?: return emptyList()
        val userRights = parentOfType<ImpExUserRights>() ?: return emptyList()
        return userRights.getValueGroups(columnNumber)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 7527291935420460513L
    }

}