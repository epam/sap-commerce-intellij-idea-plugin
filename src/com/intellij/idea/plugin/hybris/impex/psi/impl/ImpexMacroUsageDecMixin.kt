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

package com.intellij.idea.plugin.hybris.impex.psi.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroNameDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexPsiNamedElement
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexMacroReference
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpexPropertyReference
import com.intellij.idea.plugin.hybris.impex.psi.util.getKey
import com.intellij.idea.plugin.hybris.impex.psi.util.setName
import com.intellij.idea.plugin.hybris.psi.impl.ASTWrapperReferencePsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import java.io.Serial

abstract class ImpexMacroUsageDecMixin(node: ASTNode) : ASTWrapperReferencePsiElement(node), ImpexMacroUsageDec, ImpexPsiNamedElement {

    override fun createReference() = if (text.startsWith(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)) {
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