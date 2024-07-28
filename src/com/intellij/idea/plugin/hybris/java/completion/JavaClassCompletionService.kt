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
package com.intellij.idea.plugin.hybris.java.completion

import ai.grazie.utils.toLinkedSet
import com.intellij.codeInsight.completion.JavaLookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch

@Service(Service.Level.PROJECT)
class JavaClassCompletionService(val myProject: Project) {

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
            .mapNotNull { createLookupElement(it) }
            .distinctBy { it.lookupString }
            .sortedBy { it.lookupString }
            .toLinkedSet()
    }

    fun getImplementationsPsiClassesForClasses(vararg qualifiedNames: String): Set<PsiClass> {
        val psiFacade = JavaPsiFacade.getInstance(myProject)
        val allScope = GlobalSearchScope.allScope(myProject)

        return qualifiedNames
            .asSequence()
            .mapNotNull { psiFacade.findClass(it, allScope) }
            .map { ClassInheritorsSearch.search(it, allScope, true) }
            .map { it.findAll() }
            .flatten()
            .filterNot { it.isInterface }
            .distinctBy { it }
            .sortedBy { it.qualifiedName }
            .toLinkedSet()
    }

    fun createLookupElement(psiClass: PsiClass): LookupElement? {
        val presentableText = psiClass.name ?: return null
        val lookupString = psiClass.qualifiedName ?: return null

        return JavaLookupElementBuilder.forClass(psiClass, lookupString, true)
            .withPresentableText(presentableText)
    }

    companion object {
        fun getInstance(project: Project): JavaClassCompletionService = project.getService(JavaClassCompletionService::class.java)
    }

}