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

package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.apache.commons.lang3.StringUtils

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class PopulatorLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        psiClass: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (psiClass !is PsiClass) return
        val project = psiClass.project

        if (!CommonIdeaService.getInstance().isHybrisProject(project)) return

        val populatorClass = findPopulatorClass(project)
        if (populatorClass == null || populatorClass.element == null) return

        val converterFields = retrieveConverterFields(psiClass)

        if (converterFields.isEmpty()) return

        val allPopulators = ClassInheritorsSearch.search(populatorClass.element!!)

        converterFields.forEach { field ->
            val fieldName = field.name.replace("Converter", StringUtils.EMPTY)
            val candidates = allPopulators
                .filter { byName(it, fieldName) || byGenerics(it, field) }
                .map { if (byGenerics(it, field)) CandidateWrapper(0, it) else CandidateWrapper(1, it) }
                .sortedBy { it.priority }
                .map { it.psiClass }

            if (candidates.isNotEmpty()) {
                result.add(createTargetsWithGutterIcon(field.nameIdentifier, candidates))
            }
        }
    }

    private fun byName(it: PsiClass, fieldName: String) = it.name != null && it.name!!.contains(fieldName, true)

    private fun byGenerics(it: PsiClass, field: PsiField): Boolean {
        if (field.type !is PsiClassReferenceType) return false

        val interfaces = it.implementsListTypes

        if (interfaces.isEmpty()) return false
        if (interfaces.first().parameterCount != (field.type as PsiClassReferenceType).parameterCount) return false

        return interfaces.first().parameters.contentEquals((field.type as PsiClassReferenceType).parameters)
    }

    private fun retrieveConverterFields(psiClass: PsiClass): List<PsiField> {
        return psiClass.fields
            .filterNot { it.modifierList?.hasModifierProperty("static") ?: false }
            .filter { it.type is PsiClassReferenceType }
            .filter {
                (it.type as PsiClassReferenceType).reference.qualifiedName == "de.hybris.platform.servicelayer.dto.converter.Converter"
            }
    }

    private fun findPopulatorClass(project: Project): SmartPsiElementPointer<PsiClass>? {
        val populatorClass = JavaPsiFacade.getInstance(project).findClass("de.hybris.platform.converters.Populator", GlobalSearchScope.allScope(project))
            ?: return null

        return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(populatorClass)
    }

    private fun createTargetsWithGutterIcon(psiElement: PsiElement, list: Collection<PsiElement>) =
        NavigationGutterIconBuilder.create(HybrisIcons.GUTTER_POPULATOR)
            .setTargets(list)
            .setEmptyPopupText(HybrisI18NBundleUtils.message(
                    "hybris.gutter.navigate.no.matching.populators"
                ))
            .setPopupTitle(HybrisI18NBundleUtils.message(
                    "hybris.gutter.populator.class.navigate.choose.class.title"
                ))
            .setTooltipText(HybrisI18NBundleUtils.message(
                    "hybris.gutter.populator.class.tooltip.navigate.declaration"
                ))
            .createLineMarkerInfo(psiElement)

    private data class CandidateWrapper(var priority: Int, var psiClass: PsiClass)
}
