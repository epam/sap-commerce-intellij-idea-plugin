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
package com.intellij.idea.plugin.hybris.impex.completion.impl

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.impex.completion.ImpexImplementationClassCompletionContributor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch

class ImpexImplementationClassCompletionContributorImpl(val myProject: Project) : ImpexImplementationClassCompletionContributor {

    override fun getImplementationsForClass(qualifiedName: String): Set<LookupElement> {
        val clazz = JavaPsiFacade.getInstance(myProject)
            .findClass(qualifiedName, GlobalSearchScope.allScope(myProject))
            ?: return emptySet()

        return ClassInheritorsSearch
            .search(clazz, GlobalSearchScope.allScope(myProject), true)
            .findAll()
            .filter { it.qualifiedName != null && it.name != null }
            .map {
                LookupElementBuilder.create(it.qualifiedName!!)
                    .withPresentableText(it.name!!)
                    .withIcon(AllIcons.FileTypes.JavaClass)
            }
            .toSet()
    }

}