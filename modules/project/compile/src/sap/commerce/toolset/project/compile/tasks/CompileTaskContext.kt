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

package sap.commerce.toolset.project.compile.tasks

import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JavaSdkVersion
import sap.commerce.toolset.project.settings.ProjectSettings
import java.nio.file.Path

data class CompileTaskContext(
    override val context: CompileContext,
    override val platformModuleRoot: Path,
    override val bootstrapDirectory: Path,
    override val coreModuleRoot: Path,
    override val vmExecutablePath: String,
    override val settings: ProjectSettings,
   override val sdkVersion: JavaSdkVersion,
   override val platformModule: Module,
) : TaskContext
