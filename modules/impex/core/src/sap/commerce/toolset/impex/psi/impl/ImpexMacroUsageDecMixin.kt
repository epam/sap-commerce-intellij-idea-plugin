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
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.psi.ImpexMacroNameDec
import sap.commerce.toolset.impex.psi.ImpexMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpexPsiNamedElement
import sap.commerce.toolset.impex.psi.references.ImpexMacroReference
import sap.commerce.toolset.impex.psi.references.ImpexPropertyReference
import sap.commerce.toolset.impex.psi.util.getKey
import sap.commerce.toolset.impex.psi.util.setName
import sap.commerce.toolset.psi.impl.ASTWrapperReferencePsiElement
import java.io.Serial

abstract class ImpexMacroUsageDecMixin(node: ASTNode) : ASTWrapperReferencePsiElement(node), ImpexMacroUsageDec, ImpexPsiNamedElement {

    override fun createReference() = if (text.startsWith(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
        ImpexPropertyReference(this)
    } else if (text.startsWith("$")) {
        ImpexMacroReference(this)
    } else {
        null
    }

    override fun setName(newName: String) = setName(this, newName)
    override fun getName() = getKey(node)
    override fun getNameIdentifier() = this

    override fun resolveValue(evaluatedMacroUsages: MutableSet<ImpexMacroUsageDec?>): String = CachedValuesManager.getManager(project).getCachedValue(
        this,
        Key.create("SAP_CX_IMPEX_RESOLVED_VALUE_" + evaluatedMacroUsages.size),
        {
            val resolvedValue = when (val targetPsi = reference?.resolve()) {
                is ImpexMacroNameDec -> targetPsi.resolveValue(evaluatedMacroUsages)

                is Property -> targetPsi.value
                    ?: text

                else -> text
            }

            CachedValueProvider.Result.create(
                resolvedValue,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }, false
    )

    companion object {

        @Serial
        private val serialVersionUID: Long = -7539604143961775427L
    }
}