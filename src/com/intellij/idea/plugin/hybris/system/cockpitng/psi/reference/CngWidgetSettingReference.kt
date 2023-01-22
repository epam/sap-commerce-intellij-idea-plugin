/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelAccess
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.result.WidgetSettingResolveResult
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag

/**
 * See https://help.sap.com/docs/SAP_COMMERCE/5c9ea0c629214e42b727bf08800d8dfa/76d2195f994a47f593a2732ef99c91d3.html?locale=en-US&q=socket
 */
class CngWidgetSettingReference(element: PsiElement) : PsiReferenceBase.Poly<PsiElement>(element, true), PsiPolyVariantReference {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val lookingForName = value
        val widgetDefinitionId = element.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == "widget" }
            ?.getAttributeValue("widgetDefinitionId")
            ?: return emptyArray()

        return CngMetaModelAccess.getInstance(element.project).getMetaModel()
            .widgetDefinitions[widgetDefinitionId]
            ?.settings?.get(lookingForName)
            ?.retrieveDom()
            ?.let { arrayOf(WidgetSettingResolveResult(it)) }
            ?: emptyArray()
    }

}
