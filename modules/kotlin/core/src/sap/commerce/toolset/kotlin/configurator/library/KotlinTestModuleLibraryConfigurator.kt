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
package sap.commerce.toolset.kotlin.configurator.library

import com.intellij.platform.workspace.jps.entities.DependencyScope
import com.intellij.util.asSafely
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.configureLibrary
import sap.commerce.toolset.java.configurator.library.util.sources
import sap.commerce.toolset.kotlin.configurator.hasKotlinNatureExtension
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleLibraryConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCommonWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.isNonCustomModuleDescriptor
import kotlin.io.path.Path

class KotlinTestModuleLibraryConfigurator : ModuleLibraryConfigurator<YModuleDescriptor> {

    override val name: String
        get() = "Kotlin Test Sources"

    override fun isApplicable(
        context: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = context.hasKotlinNatureExtension
        && moduleDescriptor is YModuleDescriptor
        && moduleDescriptor.isNonCustomModuleDescriptor
        && context.settings.importOOTBModulesInReadOnlyMode

    override suspend fun configure(context: ProjectModuleConfigurationContext<YModuleDescriptor>) {
        val importContext = context.importContext
        val moduleDescriptor = context.moduleDescriptor
        val virtualFileUrlManager = importContext.workspace.getVirtualFileUrlManager()
        val kotlinTestSrcPath = Path(ProjectConstants.Directory.KOTLIN_TEST_SRC)
        val libraryRoots = buildList {
            addAll(moduleDescriptor.sources(virtualFileUrlManager, kotlinTestSrcPath))

            moduleDescriptor.asSafely<YCommonWebSubModuleDescriptor>()
                ?.dependantWebExtensions
                ?.forEach { dependantModuleDescriptor ->
                    addAll(dependantModuleDescriptor.sources(virtualFileUrlManager, kotlinTestSrcPath))
                }
        }

        context.moduleEntity.configureLibrary(
            context = importContext,
            moduleDescriptor = moduleDescriptor,
            libraryNameSuffix = JavaConstants.ModuleLibrary.TEST,
            scope = DependencyScope.TEST,
            libraryRoots = libraryRoots,
        )
    }
}
