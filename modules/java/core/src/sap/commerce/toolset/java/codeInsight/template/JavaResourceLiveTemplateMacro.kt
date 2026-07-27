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

import com.intellij.codeInsight.template.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.roots.ProjectRootManager

class JavaResourceLiveTemplateMacro : Macro() {

    override fun getName() = "jakartaOrJavax"

    override fun calculateResult(params: Array<out Expression>, context: ExpressionContext): Result? {
        return if (isJava21Plus(context.project)) TextResult("jakarta.annotation.Resource")
        else TextResult("javax.annotation.Resource")
    }

    fun isJava21Plus(project: Project): Boolean {
        val versionString = ProjectRootManager.getInstance(project).projectSdk?.versionString ?: return false
        val sdkVersion = JavaSdkVersion.fromVersionString(versionString) ?: return false

        return sdkVersion.isAtLeast(JavaSdkVersion.JDK_21)
    }
}
