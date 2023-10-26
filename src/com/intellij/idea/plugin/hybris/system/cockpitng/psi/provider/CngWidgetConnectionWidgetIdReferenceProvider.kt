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
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.CngWidgetReference
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.CngWidgetStubReference
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext

@Service
class CngWidgetConnectionWidgetIdReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext) =
        if (element is XmlAttributeValue)
            if (element.value.startsWith(HybrisConstants.COCKPIT_NG_WIDGET_ID_STUB, true)) arrayOf(CngWidgetStubReference(element))
            else arrayOf(CngWidgetReference(element))
        else emptyArray()

    companion object {
        val instance: PsiReferenceProvider = ApplicationManager.getApplication().getService(CngWidgetConnectionWidgetIdReferenceProvider::class.java)
    }
}