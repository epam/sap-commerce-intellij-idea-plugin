/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.AngularModuleDescriptor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VfsUtil
import org.angular2.cli.Angular2ProjectConfigurator

class AngularConfigurator {

    fun configureAfterImport(project: Project, moduleDescriptors: List<ModuleDescriptor>): List<() -> Unit> = moduleDescriptors
        .filterIsInstance<AngularModuleDescriptor>()
        .mapNotNull {
            val vfs = VfsUtil.findFileByIoFile(it.moduleRootDirectory, true)
                ?: return@mapNotNull null
            val moduleRef = ModuleManager.getInstance(project).findModuleByName(it.ideaModuleName())
                ?.let { module -> Ref.create(module) }
                ?: return@mapNotNull null

            {
                Angular2ProjectConfigurator().configureProject(project, vfs, moduleRef, true)
            }
        }

}
