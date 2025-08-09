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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.idea.plugin.hybris.util.isNotHybrisProject
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.util.concurrency.AppExecutorUtil
import org.intellij.plugins.intelliLang.Configuration
import org.intellij.plugins.intelliLang.inject.config.InjectionPlace
import org.intellij.plugins.intelliLang.inject.java.InjectionCache
import org.intellij.plugins.intelliLang.inject.java.JavaLanguageInjectionSupport

/**
 * TODO: reset Injection Cache on CRUD operation on classes related to FlexibleSearchQuery
 */
class HybrisIntelliLangStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (project.isNotHybrisProject) return

        registerJavaInjections(project)
    }

    private fun registerJavaInjections(project: Project) {
        ReadAction
            .nonBlocking<Set<String>> { findCustomExtensionsOfTheFlexibleSearch(project) }
            .finishOnUiThread(ModalityState.defaultModalityState()) {
                with(InjectionCache.getInstance(project).xmlIndex) {
                    add(HybrisConstants.CLASS_NAME_FLEXIBLE_SEARCH_QUERY)
                    addAll(it)
                }

                val targetLanguages = setOf(FlexibleSearchLanguage.id, PolyglotQueryLanguage.id)

                // TODO: replace with pattern declared in the XML file once https://youtrack.jetbrains.com/issue/IDEA-339624/ will be resolved.
                // com.intellij.patterns.compiler.PatternCompiler cannot parse primitive booleans required by the pattern
                Configuration.getInstance().getInjections(JavaLanguageInjectionSupport.JAVA_SUPPORT_ID)
                    .filter { baseInjection -> targetLanguages.contains(baseInjection.injectedLanguageId) }
                    .forEach { baseInjection ->
                        val injectionPlaces = baseInjection.injectionPlaces.toMutableSet()

                        if (injectionPlaces.any { place -> place.text.contains(HybrisConstants.CLASS_FQN_FLEXIBLE_SEARCH_QUERY) }) return@forEach

                        val psiParameterInjectionPlace = InjectionPlace(
                            PsiJavaPatterns.psiParameter().ofMethod(
                                PsiJavaPatterns.psiMethod().definedInClass(PsiJavaPatterns.psiClass().inheritorOf(false, HybrisConstants.CLASS_FQN_FLEXIBLE_SEARCH_QUERY))
                            ),
                            true
                        )
                        injectionPlaces.add(psiParameterInjectionPlace)
                        baseInjection.setInjectionPlaces(*injectionPlaces.toTypedArray())
                    }
            }
            .inSmartMode(project)
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    private fun findCustomExtensionsOfTheFlexibleSearch(project: Project) = (JavaPsiFacade.getInstance(project)
        .findClass(HybrisConstants.CLASS_FQN_FLEXIBLE_SEARCH_QUERY, GlobalSearchScope.allScope(project))
        ?.let { clazz ->
            ClassInheritorsSearch
                .search(clazz, GlobalSearchScope.allScope(project), true)
                .findAll()
                .mapNotNull { it.name }
                .toSet()
        }
        ?: emptySet())
}
