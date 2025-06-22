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
package com.intellij.idea.plugin.hybris.acl.lang.annotation

import com.intellij.idea.plugin.hybris.acl.highlighting.AclSyntaxHighlighter
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes
import com.intellij.idea.plugin.hybris.acl.psi.references.AclTSTargetAttributeReference
import com.intellij.idea.plugin.hybris.acl.psi.references.AclTSTargetTypeReference
import com.intellij.idea.plugin.hybris.lang.annotation.AbstractAnnotator
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class AclAnnotator : AbstractAnnotator(AclSyntaxHighlighter.getInstance()) {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            AclTypes.USER_RIGHTS_VALUE_GROUP_TYPE -> {
                highlightReference(
                    holder, element,
                    "hybris.inspections.acl.unresolved.type.key",
                    referenceHolder = element
                )
            }

            AclTypes.USER_RIGHTS_VALUE_TARGET -> {
                element.references.forEach {
                    val messageKey = when (it) {
                        is AclTSTargetTypeReference -> "hybris.inspections.acl.unresolved.type.key"
                        is AclTSTargetAttributeReference -> "hybris.inspections.acl.unresolved.attribute.key"
                        else -> "hybris.inspections.acl.unresolved.reference.key"
                    }

                    highlightReference(holder, element, messageKey, it)
                }
            }
        }
    }
}
