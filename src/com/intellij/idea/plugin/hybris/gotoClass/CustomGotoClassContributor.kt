/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.gotoClass

import com.intellij.ide.util.gotoByName.DefaultClassNavigationContributor
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.navigation.GotoClassContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.Processors
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter

class CustomGotoClassContributor : GotoClassContributor {

    private val defaultClassNavigationContributor = DefaultClassNavigationContributor()

    override fun getQualifiedName(item: NavigationItem) = null

    override fun getQualifiedNameSeparator() = null

    override fun getNames(project: Project, includeNonProjectItems: Boolean): Array<String> {
        if (shouldNotBeProcessed(includeNonProjectItems, project)) return emptyArray()

        val result = mutableListOf<String>()

        PsiShortNamesCache.getInstance(project).processAllClassNames(
            Processors.cancelableCollectProcessor(result),
            OotbClassesSearchScope(project),
            IdFilter.getProjectIdFilter(project, true)
        )

        return result.toTypedArray()
    }

    override fun getItemsByName(
        name: String,
        pattern: String,
        project: Project,
        includeNonProjectItems: Boolean
    ): Array<NavigationItem> {
        if (shouldNotBeProcessed(includeNonProjectItems, project)) return emptyArray()

        val result = mutableListOf<NavigationItem>()
        val scope = OotbClassesSearchScope(project)

        defaultClassNavigationContributor.processElementsWithName(
            name,
            Processors.cancelableCollectProcessor(result),
            FindSymbolParameters(pattern, pattern, scope)
        )

        return result.toTypedArray()
    }

    private fun shouldNotBeProcessed(includeNonProjectItems: Boolean, project: Project) = includeNonProjectItems
        || !ProjectSettingsComponent.getInstance(project).isHybrisProject()
}