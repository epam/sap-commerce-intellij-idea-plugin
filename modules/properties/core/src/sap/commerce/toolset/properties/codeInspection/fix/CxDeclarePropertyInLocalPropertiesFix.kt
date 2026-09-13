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
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.util.asSafely
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyScope

/**
 * Declares a key nothing in the chain declares yet, in `local.properties`, and opens it so a value can be typed.
 *
 * The value is deliberately left empty: what a missing property should be set to is the one thing this cannot know,
 * and guessing would be worse than an obvious blank.
 */
class CxDeclarePropertyInLocalPropertiesFix(private val key: String) : LocalQuickFix {

    override fun getFamilyName() = "Declare in local.properties"

    override fun getName() = "Declare '$key' in local.properties"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val localProperties = localProperties(project) ?: return
        val property = localProperties.findPropertyByKey(key)
            ?: localProperties.addProperty(key, "")

        CxPropertyCollector.getInstance(project).resetCache()

        val file = localProperties.virtualFile ?: return
        PsiNavigationSupport.getInstance()
            .createNavigatable(project, file, property.psiElement.textRange.endOffset)
            .navigate(true)
    }

    private fun localProperties(project: Project) = CxPropertyCollector.getInstance(project)
        .collect()
        .sources
        .find { it.scope == CxPropertyScope.LOCAL }
        ?.file
        ?.let { PsiManager.getInstance(project).findFile(it) }
        ?.asSafely<PropertiesFile>()
}
