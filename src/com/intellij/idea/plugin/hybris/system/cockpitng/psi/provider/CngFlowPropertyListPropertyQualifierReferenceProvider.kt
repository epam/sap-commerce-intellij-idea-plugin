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
package com.intellij.idea.plugin.hybris.system.cockpitng.psi.provider

import com.intellij.idea.plugin.hybris.java.psi.reference.JavaClassReference
import com.intellij.idea.plugin.hybris.java.psi.reference.SpringBeanJavaClassReference
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPsiHelper
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.CngFlowTSItemAttributeReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.HybrisConstants

class CngFlowPropertyListPropertyQualifierReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<PsiReference> = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        val type = CngPsiHelper.resolveContextTypeForNewItemInWizardFlow(element)
            ?: return@getCachedValue CachedValueProvider.Result.createSingleDependency(emptyArray(), PsiModificationTracker.MODIFICATION_COUNT)

        val reference = when {
            type.startsWith(HybrisConstants.COCKPIT_NG_TEMPLATE_BEAN_REFERENCE_PREFIX) ->
                SpringBeanJavaClassReference(element, type.replace(HybrisConstants.COCKPIT_NG_TEMPLATE_BEAN_REFERENCE_PREFIX, ""))

            type.contains(".") && type != HybrisConstants.COCKPIT_NG_INITIALIZE_CONTEXT_TYPE ->
                JavaClassReference(element, type)

            else -> CngFlowTSItemAttributeReference(element)
        }

        CachedValueProvider.Result.createSingleDependency(
            arrayOf(reference),
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }
}