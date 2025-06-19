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

package com.intellij.idea.plugin.hybris.acl.psi.impl

import com.intellij.idea.plugin.hybris.acl.psi.AclUserRightsValueGroupType
import com.intellij.idea.plugin.hybris.acl.psi.references.AclTypeReference
import com.intellij.idea.plugin.hybris.psi.impl.ASTWrapperReferencePsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.removeUserData
import java.io.Serial

abstract class AclUserRightsValueGroupTypeMixin(astNode: ASTNode) : ASTWrapperReferencePsiElement(astNode), AclUserRightsValueGroupType {

    override fun createReference() = AclTypeReference(this)

    override fun subtreeChanged() {
        removeUserData(AclTypeReference.CACHE_KEY)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6448164700360231834L
    }
}