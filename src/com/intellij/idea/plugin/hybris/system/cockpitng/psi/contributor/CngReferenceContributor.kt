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
package com.intellij.idea.plugin.hybris.system.cockpitng.psi.contributor

import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPatterns
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.provider.*
import com.intellij.lang.properties.PropertiesReferenceProvider
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class CngReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            CngPatterns.ITEM_TYPE,
            CngTSItemReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.FLOW_STEP_CONTENT_PROPERTY_LIST_PROPERTY_QUALIFIER,
            CngFlowPropertyListPropertyQualifierReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.FLOW_STEP_PROPERTY,
            CngFlowPropertyQualifierReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.FLOW_INITIALIZE_TYPE,
            CngFlowTypeReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.FLOW_PROPERTY_LIST_ROOT,
            CngFlowInitializePropertyReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.EDITOR_DEFINITION,
            CngEditorDefinitionReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.ITEM_ATTRIBUTE,
            CngTSItemAttributeReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.ACTION_DEFINITION,
            CngActionDefinitionReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.WIDGET_ID,
            CngWidgetReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.WIDGET_CONNECTION_WIDGET_ID,
            CngWidgetConnectionWidgetIdReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.WIDGET_DEFINITION,
            CngWidgetDefinitionReferenceProvider()
        )
        registrar.registerReferenceProvider(
            CngPatterns.WIDGET_SETTING,
            CngWidgetSettingReferenceProvider()
        )

        Plugin.PROPERTIES.ifActive {
            registrar.registerReferenceProvider(
                CngPatterns.I18N_PROPERTY,
                PropertiesReferenceProvider()
            )
        }
    }
}