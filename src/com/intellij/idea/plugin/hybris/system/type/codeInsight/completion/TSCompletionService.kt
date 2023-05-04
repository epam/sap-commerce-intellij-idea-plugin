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

package com.intellij.idea.plugin.hybris.system.type.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.openapi.project.Project

interface TSCompletionService {

    /**
     * This method should return lookup elements for possible type code, it can be Item/Enum or Relation
     */
    fun getCompletions(typeCode: String): List<LookupElementBuilder>
    fun getCompletions(typeCode: String, vararg types: TSMetaType): List<LookupElementBuilder>

    fun getCompletions(vararg types: TSMetaType): List<LookupElementBuilder>

    companion object {
        fun getInstance(project: Project): TSCompletionService = project.getService(TSCompletionService::class.java)
    }
}