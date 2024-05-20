/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexSubTypeName
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSItemReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexTSSubTypeItemReference
import com.intellij.idea.plugin.hybris.psi.impl.ASTWrapperReferencePsiElement
import com.intellij.idea.plugin.hybris.system.type.psi.reference.result.ItemResolveResult
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.removeUserData
import com.intellij.util.asSafely
import java.io.Serial

abstract class ImpexSubTypeNameMixin(node: ASTNode) : ASTWrapperReferencePsiElement(node), ImpexSubTypeName {

    override fun createReference() = if (
        text.isNotBlank()
        && headerTypeName
            ?.reference
            ?.asSafely<ImpexTSItemReference>()
            ?.multiResolve(false)
            ?.firstOrNull()
            ?.asSafely<ItemResolveResult>()
            ?.takeIf {
                it.meta.name != HybrisConstants.TS_TYPE_GENERIC_ITEM
                    &&
                    this.valueLine
                        ?.headerLine
                        ?.fullHeaderType
                        ?.modifiers
                        ?.attributeList
                        ?.firstOrNull()
                        ?.anyAttributeValue
                        ?.text != HybrisConstants.CLASS_FQN_CONFIG_IMPORT_PROCESSOR
            } != null
    ) {
        ImpexTSSubTypeItemReference(this)
    } else {
        null
    }

    override fun subtreeChanged() {
        removeUserData(ImpexTSSubTypeItemReference.CACHE_KEY)
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 3091595509597451013L
    }

}