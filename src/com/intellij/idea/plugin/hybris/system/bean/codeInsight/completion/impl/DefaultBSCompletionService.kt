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
package com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.impl

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.BSCompletionService
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.lookup.BSLookupElementFactory
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.openapi.project.Project

class DefaultBSCompletionService(private val project: Project) : BSCompletionService {

    // TODO: improve Level Mapping completions based on Global Meta Model for OCC beans
    override fun getCompletions(meta: BSGlobalMetaBean): List<LookupElement> {
        val properties = meta.allProperties.values
            .mapNotNull { BSLookupElementFactory.build(it) }
        val levelMappings = HybrisConstants.OCC_DEFAULT_LEVEL_MAPPINGS
            .map { BSLookupElementFactory.buildLevelMapping(it) }
        return properties + levelMappings
    }

}