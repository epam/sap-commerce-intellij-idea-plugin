/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.type.validation.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.model.Relation
import com.intellij.idea.plugin.hybris.system.type.validation.ItemsXmlDomValidator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.collections4.map.CaseInsensitiveMap

@Service
class RelationValidation : ItemsXmlDomValidator<Relation> {

    override fun validate(project: Project, dom: List<Relation>, psi: Map<String, PsiClass>): Boolean {
        val filteredClasses = filterRelationClasses(psi, dom)

        return dom.any { isRelationOutOfDate(it, filteredClasses) }
    }

    private fun isRelationOutOfDate(
        relation: Relation,
        filteredClasses: Map<String, PsiClass>
    ): Boolean {
        if (relation.sourceElement.navigable.value && relation.sourceElement.model.generate.value) {
            val fieldNameInTarget = relation.sourceElement.qualifier.stringValue
            val targetClassName = relation.targetElement.type.stringValue
            if (isFieldNotPresentInClass(filteredClasses, targetClassName, fieldNameInTarget)) {
                return true
            }
        }
        if (relation.targetElement.navigable.value && relation.targetElement.model.generate.value) {
            val fieldNameInSource = relation.targetElement.qualifier.stringValue
            val sourceClassName = relation.sourceElement.type.stringValue
            return isFieldNotPresentInClass(filteredClasses, sourceClassName, fieldNameInSource)
        }
        return false
    }

    private fun isFieldNotPresentInClass(
        filteredClasses: Map<String, PsiClass>,
        className: String?,
        fieldName: String?
    ): Boolean {
        if (className == null || fieldName == null) return false

        return filteredClasses[className]
            ?.allFields
            ?.find { it.name.endsWith(fieldName, true) }
            ?.let { false }
            ?: true
    }

    private fun filterRelationClasses(
        generatedClasses: Map<String, PsiClass>,
        relationsList: Collection<Relation>
    ): Map<String, PsiClass> {
        if (CollectionUtils.isEmpty(relationsList)) {
            return emptyMap()
        }
        val filteredClasses: MutableMap<String, PsiClass> = CaseInsensitiveMap()
        val relationClasses = getRelationClassNames(relationsList)

        relationClasses
            .filterNot { filteredClasses.containsKey(it) }
            .forEach { relationClass ->
                generatedClasses[relationClass + HybrisConstants.MODEL_SUFFIX]
                    ?.let { filteredClasses[relationClass] = it }

            }
        return filteredClasses
    }

    private fun getRelationClassNames(relationsList: Collection<Relation>): Set<String> {
        val sourceRelationClasses = relationsList.mapNotNull { it.sourceElement.type.stringValue }
        val targetRelationClasses = relationsList.mapNotNull { it.targetElement.type.stringValue }
        return (sourceRelationClasses + targetRelationClasses).toSet()
    }

    companion object {
        val instance: RelationValidation = ApplicationManager.getApplication().getService(RelationValidation::class.java)
    }
}
