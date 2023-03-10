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
package com.intellij.idea.plugin.hybris.diagram.module

import com.intellij.diagram.AbstractUmlVisibilityManager
import com.intellij.diagram.VisibilityLevel
import com.intellij.util.ArrayUtil

class ModuleDepDiagramVisibilityManager : AbstractUmlVisibilityManager() {

    init {
        currentVisibilityLevel = ONLY_CUSTOM_MODULES
    }

    override fun getVisibilityLevels() = LEVELS.clone()
    override fun getVisibilityLevel(o: Any?) = null
    override fun getComparator(): Comparator<VisibilityLevel> = COMPARATOR
    override fun isRelayoutNeeded() = true

    companion object {
        val ONLY_CUSTOM_MODULES = VisibilityLevel("Only Custom")
        val CUSTOM_WITH_DEPENDENCIES = VisibilityLevel("Custom with Dependencies")
        val ALL_MODULES = VisibilityLevel("All")
        private val LEVELS = arrayOf(ONLY_CUSTOM_MODULES, CUSTOM_WITH_DEPENDENCIES, ALL_MODULES)
        private val COMPARATOR = Comparator.comparingInt { level: VisibilityLevel -> ArrayUtil.indexOf(LEVELS, level) }
    }
}
