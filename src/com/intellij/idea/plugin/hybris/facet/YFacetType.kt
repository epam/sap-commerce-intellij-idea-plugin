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
package com.intellij.idea.plugin.hybris.facet

import com.intellij.facet.Facet
import com.intellij.facet.FacetType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import javax.swing.Icon

class YFacetType : FacetType<YFacet, YFacetConfiguration>(HybrisConstants.Y_FACET_TYPE_ID, FACET_ID, FACET_NAME) {

    override fun getIcon(): Icon = HybrisIcons.HYBRIS_FACET
    override fun isSuitableModuleType(type: ModuleType<*>?) = type is JavaModuleType
    override fun createDefaultConfiguration() = YFacetConfiguration()

    override fun createFacet(
        module: Module,
        name: String,
        configuration: YFacetConfiguration,
        underlyingFacet: Facet<*>?
    ) = YFacet(this, module, name, configuration, underlyingFacet)

    companion object {
        const val FACET_ID = "SAP_COMMERCE_Y_FACET_ID"
        const val FACET_NAME = "SAP Commerce"
    }
}