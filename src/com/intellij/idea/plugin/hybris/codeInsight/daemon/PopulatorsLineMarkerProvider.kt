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
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.apache.commons.lang3.StringUtils
import javax.swing.Icon

class PopulatorsLineMarkerProvider : AbstractHybrisLineMarkerProvider<PsiClass>() {

    override fun getName() = "Java - Populators"
    override fun getIcon(): Icon = HybrisIcons.GUTTER_POPULATOR
    override fun canProcess(psi: PsiFile) = true
    override fun tryCast(psi: PsiElement) = psi as? PsiClass

    override fun collectDeclarations(psi: PsiClass): Collection<RelatedItemLineMarkerInfo<PsiElement>> {
        val populatorClass = findPopulatorClass(psi.project) ?: return emptyList()
        val element = populatorClass.element ?: return emptyList()
        val converterFields = retrieveConverterFields(psi)

        if (converterFields.isEmpty()) return emptyList()

        val allPopulators = ClassInheritorsSearch.search(element)

        return converterFields
            .mapNotNull { field ->
                val fieldName = field.name.replace("Converter", StringUtils.EMPTY)
                return@mapNotNull allPopulators
                    .filter { byName(it, fieldName) || byGenerics(it, field) }
                    .map { if (byGenerics(it, field)) CandidateWrapper(0, it) else CandidateWrapper(1, it) }
                    .sortedBy { it.priority }
                    .map { it.psiClass }
                    .takeIf { it.isNotEmpty() }
                    ?.let { createTargetsWithGutterIcon(field.nameIdentifier, it) }
            }
    }

    private fun byName(it: PsiClass, fieldName: String) = it.name?.contains(fieldName, true) ?: false

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
            .filter {
                (it.type as? PsiClassReferenceType)?.reference?.qualifiedName == HybrisConstants.CLASS_CONVERTER
            }
    }

    private fun findPopulatorClass(project: Project): SmartPsiElementPointer<PsiClass>? {
        val populatorClass = JavaPsiFacade.getInstance(project).findClass(HybrisConstants.CLASS_POPULATOR, GlobalSearchScope.allScope(project))
            ?: return null

        return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(populatorClass)
    }

    private fun createTargetsWithGutterIcon(psiElement: PsiElement, targets: Collection<PsiElement>) = NavigationGutterIconBuilder
        .create(icon)
        .setTargets(targets)
        .setEmptyPopupText(message("hybris.editor.gutter.populator.class.empty.popup.text"))
        .setPopupTitle(message("hybris.editor.gutter.populator.class.popup.text"))
        .setTooltipText(message("hybris.editor.gutter.populator.class.tooltip.text"))
        .setAlignment(GutterIconRenderer.Alignment.LEFT)
        .createLineMarkerInfo(psiElement)

    private data class CandidateWrapper(var priority: Int, var psiClass: PsiClass)
}
