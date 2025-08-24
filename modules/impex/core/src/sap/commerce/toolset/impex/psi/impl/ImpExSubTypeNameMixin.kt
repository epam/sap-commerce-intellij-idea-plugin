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
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.impex.psi.ImpExSubTypeName
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.psi.references.ImpExTSItemReference
import sap.commerce.toolset.impex.psi.references.ImpExTSSubTypeItemReference
import sap.commerce.toolset.psi.impl.ASTWrapperReferencePsiElement
import sap.commerce.toolset.typeSystem.psi.reference.result.ItemResolveResult
import java.io.Serial

abstract class ImpExSubTypeNameMixin(node: ASTNode) : ASTWrapperReferencePsiElement(node), ImpExSubTypeName {

    override fun createReference() = if (
        text.isNotBlank()
        && headerTypeName
            ?.reference
            ?.asSafely<ImpExTSItemReference>()
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
        ImpExTSSubTypeItemReference(this)
    } else {
        null
    }

    override fun subtreeChanged() {
        removeUserData(ImpExTSSubTypeItemReference.CACHE_KEY)
    }

    override fun getValueLine(): ImpExValueLine? = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_VALUE_LINE, {
        val valueLine = PsiTreeUtil
            .getParentOfType(this, ImpExValueLine::class.java)

        CachedValueProvider.Result.createSingleDependency(
            valueLine,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    companion object {
        val CACHE_KEY_VALUE_LINE = Key.create<CachedValue<ImpExValueLine?>>("SAP_CX_IMPEX_VALUE_LINE")

        @Serial
        private val serialVersionUID: Long = 3091595509597451013L
    }

}