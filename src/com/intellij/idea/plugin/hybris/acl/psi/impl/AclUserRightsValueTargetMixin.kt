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

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes
import com.intellij.idea.plugin.hybris.acl.psi.AclUserRightsValueTarget
import com.intellij.idea.plugin.hybris.acl.psi.references.AclTSTargetAttributeReference
import com.intellij.idea.plugin.hybris.acl.psi.references.AclTSTargetTypeReference
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiReference
import com.intellij.psi.tree.TokenSet
import java.io.Serial

abstract class AclUserRightsValueTargetMixin(astNode: ASTNode) : ASTWrapperPsiElement(astNode), AclUserRightsValueTarget {

    private var myReferences: Array<PsiReference>? = null

    override fun getReferences(): Array<PsiReference> {
        if (myReferences == null) {
            myReferences = node.getChildren(TokenSet.create(AclTypes.FIELD_VALUE_TARGET_TYPE, AclTypes.FIELD_VALUE_TARGET_ATTRIBUTE))
                .mapNotNull {
                    when (it.elementType) {
                        AclTypes.FIELD_VALUE_TARGET_TYPE -> AclTSTargetTypeReference(this, rangeInElement = TextRange.from(it.startOffsetInParent, it.textLength))
                        AclTypes.FIELD_VALUE_TARGET_ATTRIBUTE -> AclTSTargetAttributeReference(this, rangeInElement = TextRange.from(it.startOffsetInParent, it.textLength))
                        else -> null
                    }
                }
                .toTypedArray()
        }
        return myReferences!!;
    }

    override fun subtreeChanged() {
        removeUserData(AclTSTargetAttributeReference.CACHE_KEY)
        removeUserData(AclTSTargetTypeReference.CACHE_KEY)
        myReferences = null;
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -7695829757588807139L
    }

}