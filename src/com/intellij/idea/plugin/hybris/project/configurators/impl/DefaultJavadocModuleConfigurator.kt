/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.project.configurators.JavadocModuleConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.CCv2ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.ConfigModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCustomRegularModuleDescriptor
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.roots.JavaModuleExternalPaths
import com.intellij.openapi.roots.ModifiableRootModel

class DefaultJavadocModuleConfigurator : JavadocModuleConfigurator {

    override fun configure(
        indicator: ProgressIndicator,
        modifiableRootModel: ModifiableRootModel,
        moduleDescriptor: ModuleDescriptor,
        appSettings: HybrisApplicationSettings
    ) {
        indicator.text2 = HybrisI18NBundleUtils.message("hybris.project.import.module.javadoc")

        val javadocRefList = mutableListOf<String>()
        val javaModuleExternalPaths = modifiableRootModel.getModuleExtension(JavaModuleExternalPaths::class.java)

        if (PluginCommon.isPluginActive(PluginCommon.MAVEN_PLUGIN_ID)) {
            MavenUtils.resolveMavenJavadocs(modifiableRootModel, moduleDescriptor, indicator, appSettings)
                .map { "jar://$it!/" }
                .let { javadocRefList.addAll(it) }

            javadocRefList.sort()
        }

        moduleDescriptor.rootProjectDescriptor.getJavadocUrl()
            ?.takeUnless { moduleDescriptor is YCustomRegularModuleDescriptor }
            ?.takeUnless { moduleDescriptor is CCv2ModuleDescriptor }
            ?.takeUnless { moduleDescriptor is ConfigModuleDescriptor }
            ?.let { javadocRefList.add(it) }

        javaModuleExternalPaths.javadocUrls = javadocRefList.toTypedArray()
    }
}
