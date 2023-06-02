/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.project.factories

import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.RootModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YConfigModuleDescriptor
import com.intellij.idea.plugin.hybris.project.exceptions.HybrisConfigurationException
import com.intellij.openapi.application.ApplicationManager
import java.io.File

interface ModuleDescriptorFactory {

    @Throws(HybrisConfigurationException::class)
    fun createDescriptor(file: File, rootProjectDescriptor: HybrisProjectDescriptor): ModuleDescriptor

    @Throws(HybrisConfigurationException::class)
    fun createRootDescriptor(
        rootDirectory: File,
        rootProjectDescriptor: HybrisProjectDescriptor,
        name: String
    ): RootModuleDescriptor

    @Throws(HybrisConfigurationException::class)
    fun createConfigDescriptor(rootDirectory: File, rootProjectDescriptor: HybrisProjectDescriptor, name: String): YConfigModuleDescriptor

    companion object {
        @JvmStatic
        val instance: ModuleDescriptorFactory = ApplicationManager.getApplication().getService(ModuleDescriptorFactory::class.java)
    }
}
