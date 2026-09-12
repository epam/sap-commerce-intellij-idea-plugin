/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.codeInspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyScope

/**
 * Moves a declaration out of an extension and into `local.properties`, where a developer's own environment belongs.
 *
 * An existing declaration of the same key in `local.properties` is updated rather than duplicated, since two
 * declarations of one key in a single file only confuse the reader — the later one wins either way.
 */
class CxMovePropertyToLocalPropertiesFix : LocalQuickFix {

    override fun getFamilyName() = "Move to local.properties"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val property = PsiTreeUtil.getParentOfType(descriptor.psiElement, Property::class.java, false)
            ?: descriptor.psiElement.asSafely<Property>()
            ?: return
        val key = property.key ?: return
        val value = property.value ?: ""
        val localProperties = localProperties(project) ?: return

        localProperties.findPropertyByKey(key)
            ?.setValue(value)
            ?: localProperties.addProperty(key, value)

        property.delete()

        CxPropertyCollector.getInstance(project).resetCache()
    }

    private fun localProperties(project: Project) = CxPropertyCollector.getInstance(project)
        .collect()
        .sources
        .find { it.scope == CxPropertyScope.LOCAL }
        ?.file
        ?.let { PsiManager.getInstance(project).findFile(it) }
        ?.asSafely<PropertiesFile>()
}
