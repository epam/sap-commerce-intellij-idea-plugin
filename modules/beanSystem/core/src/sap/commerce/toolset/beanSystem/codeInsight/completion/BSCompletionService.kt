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

package sap.commerce.toolset.beanSystem.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean

@Service(Service.Level.PROJECT)
class BSCompletionService(private val project: Project) {

    fun getCompletions(meta: BSGlobalMetaBean): List<LookupElement> {
        val properties = meta.allProperties.values
            .mapNotNull { _root_ide_package_.sap.commerce.toolset.beanSystem.codeInsight.lookup.BSLookupElementFactory.build(it) }
        val levelMappings = HybrisConstants.OCC_DEFAULT_LEVEL_MAPPINGS
            .map { _root_ide_package_.sap.commerce.toolset.beanSystem.codeInsight.lookup.BSLookupElementFactory.buildLevelMapping(it) }
        return properties + levelMappings
    }

    fun getCompletions(vararg types: sap.commerce.toolset.beanSystem.meta.model.BSMetaType) = with(_root_ide_package_.sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess.Companion.getInstance(project)) {
        types
            .map { metaType ->
                when (metaType) {
                    _root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_ENUM -> this
                        .getAll<sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaEnum>(metaType)
                        .mapNotNull { _root_ide_package_.sap.commerce.toolset.beanSystem.codeInsight.lookup.BSLookupElementFactory.build(it) }

                    _root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_BEAN -> this
                        .getAll<sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean>(metaType)
                        .mapNotNull { _root_ide_package_.sap.commerce.toolset.beanSystem.codeInsight.lookup.BSLookupElementFactory.build(it, metaType) }

                    _root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_WS_BEAN -> this
                        .getAll<sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean>(metaType)
                        .mapNotNull { _root_ide_package_.sap.commerce.toolset.beanSystem.codeInsight.lookup.BSLookupElementFactory.build(it, metaType) }

                    _root_ide_package_.sap.commerce.toolset.beanSystem.meta.model.BSMetaType.META_EVENT -> this
                        .getAll<sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean>(metaType)
                        .mapNotNull { _root_ide_package_.sap.commerce.toolset.beanSystem.codeInsight.lookup.BSLookupElementFactory.build(it, metaType) }
                }
            }
            .flatten()
    }

    companion object {
        fun getInstance(project: Project): BSCompletionService = project.service()
    }
}