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
import com.intellij.facet.FacetManager
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.module.Module
import java.io.Serial

class YFacet(
    facetType: YFacetType,
    module: Module,
    name: String,
    configuration: YFacetConfiguration,
    underlyingFacet: Facet<*>?
) : Facet<YFacetConfiguration>(facetType, module, name, configuration, underlyingFacet) {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -112372831447187023L

        fun get(module: Module): YFacet? {
            if (module.isDisposed) return null
            return FacetManager.getInstance(module).getFacetByType(HybrisConstants.Y_FACET_TYPE_ID)
        }

        fun getState(module: Module) = get(module)
            ?.configuration
            ?.state
    }
}