/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.apache.commons.lang3.StringUtils

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class HybrisPopulatorLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(psiClass: PsiElement,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (psiClass is PsiClass) {
            val project = psiClass.project

            if (isNotHybrisProject(project)) {
                return
            }

            val populatorClass = findPopulatorClass(project)
            if (populatorClass == null || populatorClass.element == null) {
                return
            }

            val converterFields = retrieveConverterFields(psiClass)
            if (converterFields.isNotEmpty()) {
                val allPopulators = ClassInheritorsSearch.search(populatorClass.element!!)

                converterFields.forEach { field ->
                    val fieldName = field.name.replace("Converter", StringUtils.EMPTY)
                    val candidates = allPopulators
                            .filter { byName(it, fieldName) || byGenerics(it, field) }
                            .map { if (byGenerics(it, field)) CandidateWrapper(0, it) else CandidateWrapper(1, it) }
                            .sortedBy { it.priority }
                            .map { it.psiClass }

                    if (candidates.isNotEmpty()) {
                        createTargetsWithGutterIcon(result, field.nameIdentifier, candidates)
                    }
                }

            }
        }
    }

    private fun byName(it: PsiClass, fieldName: String) = it.name != null && it.name!!.contains(fieldName, true)

    private fun byGenerics(it: PsiClass, field: PsiField): Boolean {
        val interfaces = it.implementsListTypes
        if (interfaces.isNotEmpty()) {
            if (field.type is PsiClassReferenceType) {
                if (interfaces.first().parameterCount == (field.type as PsiClassReferenceType).parameterCount) {
                    return interfaces.first().parameters.contentEquals((field.type as PsiClassReferenceType).parameters)
                }
            }
        }

        return false
    }

    private fun retrieveConverterFields(psiClass: PsiClass): List<PsiField> {
        return psiClass.fields
                .filter { it.isNotStatic() }
                .filter {
                    it.type is PsiClassReferenceType
                            && (it.type as PsiClassReferenceType).reference.qualifiedName == "de.hybris.platform.servicelayer.dto.converter.Converter"
                }
    }

    private fun isNotHybrisProject(project: Project) =
            !HybrisProjectSettingsComponent.getInstance(project).state.isHybrisProject

    private fun findPopulatorClass(project: Project): SmartPsiElementPointer<PsiClass>? {
        val populatorClass = JavaPsiFacade.getInstance(project).findClass("de.hybris.platform.converters.Populator", GlobalSearchScope.allScope(project))
        if (populatorClass != null) {
            return SmartPointerManager.getInstance(project).createSmartPsiElementPointer<PsiClass>(populatorClass)
        }
        return null
    }

    private fun createTargetsWithGutterIcon(
            result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>,
            psiElement: PsiElement,
            list: Collection<PsiElement>
    ) {
        val builder = NavigationGutterIconBuilder.create(HybrisIcons.GUTTER_POPULATOR).setTargets(list)

        builder.setEmptyPopupText(HybrisI18NBundleUtils.message(
                "hybris.gutter.navigate.no.matching.populators"
        ))

        builder.setPopupTitle(HybrisI18NBundleUtils.message(
                "hybris.gutter.populator.class.navigate.choose.class.title"
        ))
        builder.setTooltipText(HybrisI18NBundleUtils.message(
                "hybris.gutter.populator.class.tooltip.navigate.declaration"
        ))
        result.add(builder.createLineMarkerInfo(psiElement))
    }

    private data class CandidateWrapper(var priority: Int, var psiClass: PsiClass)
}

private fun PsiField.isNotStatic() = !this.hasModifier(JvmModifier.STATIC)