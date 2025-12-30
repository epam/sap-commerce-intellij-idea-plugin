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

package sap.commerce.toolset.project.module

import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.context.ModuleGroup
import sap.commerce.toolset.project.context.ModuleRoot
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.util.fileExists
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name

class YPlatformModuleRootResolver : ModuleRootResolver {

    private val hybrisBinPlatform by lazy { Path("hybris").resolve("bin").resolve("platform") }

    override fun isApplicable(path: Path) = with(path) {
        name == EiConstants.Extension.PLATFORM
            && resolve(HybrisConstants.EXTENSIONS_XML).fileExists
            && endsWith(hybrisBinPlatform)
    }

    override fun resolve(path: Path): ResolvedModuleRoot = ResolvedModuleRoot(
        FileVisitResult.CONTINUE,
        ModuleRoot(ModuleGroup.HYBRIS, ModuleDescriptorType.PLATFORM, path)
    )
}