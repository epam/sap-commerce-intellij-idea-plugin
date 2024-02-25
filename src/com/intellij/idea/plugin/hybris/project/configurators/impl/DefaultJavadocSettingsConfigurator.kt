/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.project.configurators.JavadocSettingsConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.CCv2ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.ConfigModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCustomRegularModuleDescriptor
import com.intellij.openapi.roots.JavaModuleExternalPaths
import com.intellij.openapi.roots.ModifiableRootModel

class DefaultJavadocSettingsConfigurator : JavadocSettingsConfigurator {
    override fun configure(modifiableRootModel: ModifiableRootModel, moduleDescriptor: ModuleDescriptor) {
        val javadocRefList = mutableListOf<String>()
        val javaModuleExternalPaths = modifiableRootModel.getModuleExtension(JavaModuleExternalPaths::class.java)

        moduleDescriptor.rootProjectDescriptor.getJavadocUrl()
            ?.takeUnless { moduleDescriptor is YCustomRegularModuleDescriptor }
             ?.takeUnless { moduleDescriptor is CCv2ModuleDescriptor }
            ?.takeUnless { moduleDescriptor is ConfigModuleDescriptor }
            ?.let { javadocRefList.add(it) }

        javaModuleExternalPaths.javadocUrls = javadocRefList.toTypedArray()
    }
}