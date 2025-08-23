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

package sap.commerce.toolset.java.configurator

import com.intellij.compiler.CompilerConfiguration
import com.intellij.compiler.CompilerConfigurationImpl
import com.intellij.util.asSafely
import org.jetbrains.jps.model.java.compiler.JavaCompilers
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor

class JavaCompilerConfigurator : ProjectPostImportConfigurator {

    override val name: String
        get() = "Java Compiler"

    override fun postImport(
        hybrisProjectDescriptor: HybrisProjectDescriptor
    ): List<() -> Unit> {
        val project = hybrisProjectDescriptor.project ?: return emptyList()
        val compilerConfiguration = CompilerConfiguration.getInstance(project)
            .asSafely<CompilerConfigurationImpl>() ?: return emptyList()
        val compilerVersion = PropertyService.getInstance(project).findProperty(HybrisConstants.PROPERTY_BUILD_COMPILER)
            ?: return emptyList()

        return listOf(
            {
                when (compilerVersion) {
                    "org.eclipse.jdt.core.JDTCompilerAdapter" -> applyCompiler(compilerConfiguration, JavaCompilers.ECLIPSE_ID)
                    "modern" -> applyCompiler(compilerConfiguration, JavaCompilers.JAVAC_ID)
                }
            }
        )
    }

    private fun applyCompiler(compilerConfiguration: CompilerConfigurationImpl, id: String) {
        compilerConfiguration.registeredJavaCompilers
            .firstOrNull { id == it.id }
            .let { compilerConfiguration.defaultCompiler = it }
    }
}