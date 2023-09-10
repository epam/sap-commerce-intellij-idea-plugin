/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.java.psi.reference.JavaClassReference
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPsiHelper
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.CngFlowTSItemAttributeReference
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class CngFlowPropertyQualifierReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<PsiReference> {
        val type = CngPsiHelper.resolveContextTypeForNewItemInWizardFlow(element)
            ?: return emptyArray()

        return if (type.contains(".")
            && type != HybrisConstants.COCKPIT_NG_INITIALIZE_CONTEXT_TYPE
            && PluginCommon.isPluginActive(PluginCommon.JAVA_PLUGIN_ID)
        ) arrayOf(JavaClassReference(element, type))
        else arrayOf(CngFlowTSItemAttributeReference(element))
    }

    companion object {
        val instance: PsiReferenceProvider = ApplicationManager.getApplication().getService(CngFlowPropertyQualifierReferenceProvider::class.java)
    }
}