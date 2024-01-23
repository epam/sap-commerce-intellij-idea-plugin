/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.facet.FacetType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YSubModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCustomRegularModuleDescriptor
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.zeroturnaround.javarebel.idea.plugin.actions.ToggleRebelFacetAction
import com.zeroturnaround.javarebel.idea.plugin.facet.JRebelFacet
import com.zeroturnaround.javarebel.idea.plugin.facet.JRebelFacetType
import com.zeroturnaround.javarebel.idea.plugin.xml.RebelXML
import org.apache.commons.io.IOUtils
import org.zeroturnaround.jrebel.client.config.JRebelConfiguration
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class JRebelConfigurator {

    private val logger = Logger.getInstance(JRebelConfigurator::class.java)

    fun configureAfterImport(project: Project, moduleDescriptors: List<ModuleDescriptor>): List<() -> Unit> = moduleDescriptors
        .filter {
            it is YCustomRegularModuleDescriptor
                || (it is YSubModuleDescriptor && it.owner is YCustomRegularModuleDescriptor)
        }
        .mapNotNull { ModuleManager.getInstance(project).findModuleByName(it.ideaModuleName()) }
        .mapNotNull { configure(it) }

    fun fixBackOfficeJRebelSupport(project: Project) {
        val hybrisProjectSettings = HybrisProjectSettingsComponent.getInstance(project).state
        val compilingXml = File(
            FileUtilRt.toSystemDependentName(
                project.basePath + "/" + hybrisProjectSettings.hybrisDirectory
                    + HybrisConstants.PLATFORM_MODULE_PREFIX + HybrisConstants.ANT_COMPILING_XML
            )
        )
        if (!compilingXml.isFile) return

        var content = try {
            IOUtils.toString(FileInputStream(compilingXml), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            logger.error(e)
            return
        }
        if (!content.contains("excludes=\"**/rebel.xml\"")) {
            return
        }
        content = content.replace("excludes=\"**/rebel.xml\"", "")
        try {
            IOUtils.write(content, FileOutputStream(compilingXml), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    private fun configure(javaModule: Module): (() -> Unit)? {
        val facet = JRebelFacet.getInstance(javaModule)

        if (facet != null) return null

        val facetType = FacetType.findInstance(JRebelFacetType::class.java)

        if (!facetType.isSuitableModuleType(ModuleType.get(javaModule))) return null

        return {
            // To ensure regeneration of the rebel.xml,
            // we may need to remove backup created by the JRebel plugin on module removal during the Project Refresh.
            val xml = RebelXML.getInstance(javaModule)
            val backupHash = xml.backupHash()

            val backupDirectory = File(JRebelConfiguration.getUserHomeDir(), "xml-backups/$backupHash")
            if (backupDirectory.exists() && backupDirectory.isDirectory) {
                FileUtil.deleteRecursively(backupDirectory.toPath())
            }

            ToggleRebelFacetAction.conditionalEnableJRebelFacet(javaModule, false, false)
        }
    }

}