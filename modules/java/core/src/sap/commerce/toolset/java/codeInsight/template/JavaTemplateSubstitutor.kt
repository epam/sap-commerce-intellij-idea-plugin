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
package sap.commerce.toolset.java.codeInsight.template

import com.intellij.codeInsight.template.TemplateSubstitutor
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSubstitutionContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.roots.ProjectRootManager
import sap.commerce.toolset.java.JavaConstants

class JavaTemplateSubstitutor : TemplateSubstitutor {

    override fun substituteTemplate(
        substitutionContext: TemplateSubstitutionContext,
        template: TemplateImpl
    ): TemplateImpl? {
        if (template.key != JavaConstants.LiveTemplates.YSRI) return null
        if (!isJava21Plus(substitutionContext.project)) return null

        return template.copy()
            .apply {
                this.string = this.string.replace(
                    "javax.annotation.Resource",
                    "jakarta.annotation.Resource"
                )
            }
    }

    fun isJava21Plus(project: Project): Boolean {
        val versionString = ProjectRootManager.getInstance(project).projectSdk?.versionString ?: return false
        val sdkVersion = JavaSdkVersion.fromVersionString(versionString) ?: return false

        return sdkVersion.isAtLeast(JavaSdkVersion.JDK_21)
    }
}
