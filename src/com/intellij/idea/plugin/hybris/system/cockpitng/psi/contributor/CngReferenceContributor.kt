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
package com.intellij.idea.plugin.hybris.system.cockpitng.psi.contributor

import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPatterns
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.provider.*
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class CngReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            CngPatterns.ITEM_TYPE,
            CngTSItemReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.FLOW_STEP_CONTENT_PROPERTY_QUALIFIER,
            CngFlowPropertyQualifierReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.FLOW_INITIALIZE_TYPE,
            CngFlowTypeReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.FLOW_PROPERTY_LIST_ROOT,
            CngFlowInitializePropertyReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.EDITOR_DEFINITION,
            CngEditorDefinitionReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.ITEM_ATTRIBUTE,
            CngTSItemAttributeReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.ACTION_DEFINITION,
            CngActionDefinitionReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.WIDGET_ID,
            CngWidgetReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.WIDGET_CONNECTION_WIDGET_ID,
            CngWidgetConnectionWidgetIdReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.WIDGET_DEFINITION,
            CngWidgetDefinitionReferenceProvider.instance
        )
        registrar.registerReferenceProvider(
            CngPatterns.WIDGET_SETTING,
            CngWidgetSettingReferenceProvider.instance
        )
    }
}