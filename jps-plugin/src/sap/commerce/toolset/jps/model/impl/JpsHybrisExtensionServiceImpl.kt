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

package sap.commerce.toolset.jps.model.impl

import org.jetbrains.jps.incremental.resources.ResourcesBuilder
import sap.commerce.toolset.jps.model.JpsHybrisExtensionService
import sap.commerce.toolset.jps.model.JpsHybrisFacetElementChildRole

class JpsHybrisExtensionServiceImpl : JpsHybrisExtensionService {

    init {
        /*
            do not copy <extension>/resources -> <extension>/<out directory> when module has SAP Commerce Facet
            <out directory> can be "classes" or "eclipsebin"

            used in the "ResourcesBuilder.isResourceProcessingEnabled"
         */
        ResourcesBuilder.registerEnabler { module ->
            val facetSettings = module.container.getChild(JpsHybrisFacetElementChildRole.INSTANCE)
                ?.settings
                ?: return@registerEnabler true

            when {
                facetSettings.readonly -> false
                facetSettings.backofficeModule && facetSettings.subType == "BACKOFFICE" -> true
                else -> false
            }
        }
    }
}