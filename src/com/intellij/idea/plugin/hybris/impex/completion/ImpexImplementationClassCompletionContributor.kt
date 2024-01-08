/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.idea.plugin.hybris.impex.codeInsight.lookup.ImpExLookupElementFactory
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch

@Service(Service.Level.PROJECT)
class ImpexImplementationClassCompletionContributor(val myProject: Project) {

    fun getImplementationsForClasses(vararg qualifiedNames: String): Set<LookupElement> {
        val psiFacade = JavaPsiFacade.getInstance(myProject)
        val allScope = GlobalSearchScope.allScope(myProject)

        return qualifiedNames
            .asSequence()
            .mapNotNull { psiFacade.findClass(it, allScope) }
            .map { ClassInheritorsSearch.search(it, allScope, true) }
            .map { it.findAll() }
            .flatten()
            .filterNot { it.isInterface }
            .mapNotNull {
                val presentableText = it.name ?: return@mapNotNull null
                ImpExLookupElementFactory.buildJavaClass(it, presentableText)
            }
            .toSet()
    }

    companion object {
        fun getInstance(project: Project): ImpexImplementationClassCompletionContributor = project.getService(ImpexImplementationClassCompletionContributor::class.java)
    }

}