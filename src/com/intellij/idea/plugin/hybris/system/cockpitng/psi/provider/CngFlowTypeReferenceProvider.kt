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
package com.intellij.idea.plugin.hybris.system.cockpitng.psi.provider

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.CngFlowTSItemReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext

class CngFlowTypeReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<CngFlowTSItemReference> = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        val references = (element as? XmlAttributeValue)
            // type may point to Java class
            ?.takeUnless { it.value.contains(".") && it.value != HybrisConstants.COCKPIT_NG_INITIALIZE_CONTEXT_TYPE }
            ?.let { arrayOf(CngFlowTSItemReference(element)) }
            ?: emptyArray()

        CachedValueProvider.Result.createSingleDependency(
            references,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }

}