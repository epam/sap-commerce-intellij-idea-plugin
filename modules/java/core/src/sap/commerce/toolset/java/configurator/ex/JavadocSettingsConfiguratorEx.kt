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

package sap.commerce.toolset.java.configurator.ex

import com.intellij.openapi.roots.JavaModuleExternalPaths
import com.intellij.openapi.roots.ModifiableRootModel
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCustomRegularModuleDescriptor

internal object JavadocSettingsConfiguratorEx {

    fun configure(modifiableRootModel: ModifiableRootModel, moduleDescriptor: ModuleDescriptor) {
        val javadocRefList = mutableListOf<String>()
        val javaModuleExternalPaths = modifiableRootModel.getModuleExtension(JavaModuleExternalPaths::class.java)

        moduleDescriptor.rootProjectDescriptor.javadocUrl
            ?.takeUnless { moduleDescriptor is YCustomRegularModuleDescriptor }
            ?.takeUnless { moduleDescriptor is ConfigModuleDescriptor }
            ?.let { javadocRefList.add(it) }

        javaModuleExternalPaths.javadocUrls = javadocRefList.toTypedArray()
    }
}