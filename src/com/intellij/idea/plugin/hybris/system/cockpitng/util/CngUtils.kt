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

package com.intellij.idea.plugin.hybris.system.cockpitng.util

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelStateService
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Context
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.MergeMode
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

object CngUtils {

    val operatorValues = setOf(
        "equals",
        "unequal",
        "startsWith",
        "endsWith",
        "contains",
        "doesNotContain",
        "like",
        "greater",
        "greaterOrEquals",
        "less",
        "lessOrEquals",
        "in",
        "notIn",
        "exists",
        "notExists",
        "isEmpty",
        "isNotEmpty",
        "or",
        "and",
        "match",
    )
    val mergeModes by lazy {
        MergeMode.entries.map { it.value }
    }

    fun isConfigFile(file: XmlFile) = DomManager.getDomManager(file.project).getFileElement(file, Config::class.java) != null

    fun getValidMergeByValues(project: Project) = project.service<CngMetaModelStateService>().get()
        .contextAttributes
        .keys
        // exclude itself
        .filter { Context.MERGE_BY != it }

}