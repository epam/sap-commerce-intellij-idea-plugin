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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.facet.ModifiableFacetModel
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.kotlin.yExtensionName
import com.intellij.idea.plugin.hybris.project.configurators.SpringConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.spring.facet.SpringFacet
import org.apache.commons.lang3.StringUtils
import org.jdom.Element
import org.jdom.JDOMException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

// TODO: improve sub-modules support
class DefaultSpringConfigurator : SpringConfigurator {

    override fun findSpringConfiguration(hybrisProjectDescriptor: HybrisProjectDescriptor, yModuleDescriptors: Map<String, YModuleDescriptor>) {
        val localProperties = hybrisProjectDescriptor.configHybrisModuleDescriptor
            ?.let { File(it.moduleRootDirectory, HybrisConstants.LOCAL_PROPERTIES) }

        val advancedProperties = File(hybrisProjectDescriptor.platformHybrisModuleDescriptor.moduleRootDirectory, HybrisConstants.ADVANCED_PROPERTIES)
        for (moduleDescriptor in yModuleDescriptors.values) {
            processHybrisModule(yModuleDescriptors, moduleDescriptor)

            if (moduleDescriptor is YCoreExtModuleDescriptor) {
                moduleDescriptor.springFileSet.add(advancedProperties.absolutePath)

                localProperties
                    ?.let { moduleDescriptor.springFileSet.add(it.absolutePath) }
            }
        }
    }

    override fun configureDependencies(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        allYModules: MutableMap<String, YModuleDescriptor>,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val modifiableFacetModelMap = modifiableModelsProvider.modules
            .associate { it.yExtensionName() to modifiableModelsProvider.getModifiableFacetModel(it) }

        allYModules.values
            .forEach { yModule ->
                val dependencies = yModule.dependenciesTree
                    .takeIf { it.isNotEmpty() }
                    ?: setOf(hybrisProjectDescriptor.platformHybrisModuleDescriptor)
                configureFacetDependencies(yModule, modifiableFacetModelMap, dependencies)
            }

        configureFacetDependencies(
            hybrisProjectDescriptor.platformHybrisModuleDescriptor,
            modifiableFacetModelMap,
            allYModules.values
                .filterIsInstance<YPlatformExtModuleDescriptor>()
                .toSet()
        )
    }

    private fun configureFacetDependencies(
        moduleDescriptor: ModuleDescriptor,
        modifiableFacetModelMap: Map<String, ModifiableFacetModel>,
        dependencies: Set<ModuleDescriptor>
    ) {
        val springFileSet = getSpringFileSet(modifiableFacetModelMap, moduleDescriptor)
            ?: return

        dependencies
            .sorted()
            .mapNotNull { getSpringFileSet(modifiableFacetModelMap, it) }
            .forEach { springFileSet.addDependency(it) }
    }

    private fun getSpringFileSet(
        modifiableFacetModelMap: Map<String, ModifiableFacetModel>,
        moduleDescriptor: ModuleDescriptor
    ) = modifiableFacetModelMap[moduleDescriptor.name]
        ?.getFacetByType(SpringFacet.FACET_TYPE_ID)
        ?.fileSets
        ?.takeIf { it.isNotEmpty() }
        ?.iterator()
        ?.next()

    private fun processHybrisModule(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor
    ) {
        try {
            when (moduleDescriptor) {
                is YRegularModuleDescriptor -> processPropertiesFile(moduleDescriptorMap, moduleDescriptor)
                is YWebSubModuleDescriptor -> processWebXml(moduleDescriptorMap, moduleDescriptor)
                is YBackofficeSubModuleDescriptor -> processBackofficeSubModule(moduleDescriptorMap, moduleDescriptor)
            }
        } catch (e: Exception) {
            LOG.error("Unable to parse Spring context for module " + moduleDescriptor.name, e)
        }
    }

    private fun processPropertiesFile(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor
    ) {
        val projectProperties = Properties()
        val propFile = File(moduleDescriptor.moduleRootDirectory, HybrisConstants.PROJECT_PROPERTIES)
        moduleDescriptor.springFileSet.add(propFile.absolutePath)
        try {
            projectProperties.load(propFile.inputStream())
        } catch (e: FileNotFoundException) {
            return
        } catch (e: IOException) {
            LOG.error("", e)
            return
        }
        projectProperties.stringPropertyNames()
            .filter {
                it.endsWith(HybrisConstants.APPLICATION_CONTEXT_SPRING_FILES)
                    || it.endsWith(HybrisConstants.ADDITIONAL_WEB_SPRING_CONFIG_FILES)
                    || it.endsWith(HybrisConstants.GLOBAL_CONTEXT_SPRING_FILES)

            }
            .forEach { key ->
                val moduleName = key.substring(0, key.indexOf('.'))
                // relevantModule can be different to a moduleDescriptor. e.g. addon concept
                moduleDescriptorMap[moduleName]
                    ?.let { relevantModule ->
                        projectProperties.getProperty(key)
                            .split(",")
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                            .filterNot { addSpringXmlFile(moduleDescriptorMap, relevantModule, getResourceDir(relevantModule), it) }
                            .forEach {
                                val dir = hackGuessLocation(relevantModule)
                                addSpringXmlFile(moduleDescriptorMap, relevantModule, dir, it)
                            }
                    }
            }
    }

    // This is not a nice practice but the platform has a bug in acceleratorstorefrontcommons/project.properties.
    // See https://jira.hybris.com/browse/ECP-3167
    private fun hackGuessLocation(moduleDescriptor: YModuleDescriptor) = File(
        getResourceDir(moduleDescriptor),
        FileUtilRt.toSystemDependentName(moduleDescriptor.name + "/web/spring")
    )

    private fun processBackofficeSubModule(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YBackofficeSubModuleDescriptor
    ) {
        File(moduleDescriptor.owner.moduleRootDirectory, HybrisConstants.RESOURCES_DIRECTORY)
            .listFiles { _, name: String -> name.endsWith("-backoffice-spring.xml") }
            ?.forEach { processSpringFile(moduleDescriptorMap, moduleDescriptor, it) }
    }

    @Throws(IOException::class, JDOMException::class)
    private fun processWebXml(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YWebSubModuleDescriptor
    ) {
        File(moduleDescriptor.moduleRootDirectory, HybrisConstants.WEBROOT_WEBINF_WEB_XML_PATH)
            .takeIf { it.exists() }
            ?.let { getDocumentRoot(it) }
            ?.takeUnless { it.isEmpty || it.name != "web-app" }
            ?.children
            ?.asSequence()
            ?.filter { it.name == "context-param" }
            ?.filter { it.children.any { p: Element -> p.name == "param-name" && p.value == "contextConfigLocation" } }
            ?.mapNotNull { it.children.firstOrNull { p: Element -> p.name == "param-value" } }
            ?.map { it.value }
            ?.map { location -> location.trim { it <= ' ' } }
            ?.firstOrNull()
            ?.let { processContextParam(moduleDescriptorMap, moduleDescriptor, it) }

    }

    private fun processContextParam(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        contextConfigLocation: String
    ) {
        val webModuleDir = File(moduleDescriptor.moduleRootDirectory, HybrisConstants.WEB_WEBROOT_DIRECTORY_PATH)

        SPLIT_PATTERN.split(contextConfigLocation)
            .filter { it.endsWith(".xml") }
            .map { File(webModuleDir, it) }
            .filter { it.exists() }
            .forEach { processSpringFile(moduleDescriptorMap, moduleDescriptor, it) }
    }

    @Throws(IOException::class, JDOMException::class)
    private fun getDocumentRoot(inputFile: File) = JDOMUtil.load(inputFile)

    @Throws(IOException::class, JDOMException::class)
    private fun hasSpringContent(springFile: File) = with(getDocumentRoot(springFile)) {
        !this.isEmpty && this.name == "beans"
    }

    private fun processSpringFile(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        relevantModule: YModuleDescriptor,
        springFile: File
    ): Boolean {
        try {
            if (!hasSpringContent(springFile)) return false

            if (relevantModule.springFileSet.add(springFile.absolutePath)) {
                scanForSpringImport(moduleDescriptorMap, relevantModule, springFile)
            }
            return true
        } catch (e: Exception) {
            LOG.error("unable scan file for spring imports " + springFile.name)
        }
        return false
    }

    @Throws(IOException::class, JDOMException::class)
    private fun scanForSpringImport(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        springFile: File
    ) {
        getDocumentRoot(springFile).children
            .filter { it.name == "import" }
            .forEach { processImportNodeList(moduleDescriptorMap, moduleDescriptor, it, springFile) }
    }

    private fun processImportNodeList(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        import: Element,
        springFile: File
    ) {
        val resource = import.getAttributeValue("resource")

        if (resource.startsWith("classpath:")) {
            addSpringOnClasspath(moduleDescriptorMap, moduleDescriptor, resource.substring("classpath:".length))
        } else {
            addSpringXmlFile(moduleDescriptorMap, moduleDescriptor, springFile.parentFile, resource)
        }
    }

    private fun addSpringOnClasspath(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        relevantModule: YModuleDescriptor,
        fileOnClasspath: String
    ) {
        val resourceDirectory = getResourceDir(relevantModule)
        if (addSpringXmlFile(moduleDescriptorMap, relevantModule, resourceDirectory, fileOnClasspath)) return

        val file = StringUtils.stripStart(fileOnClasspath, "/")

        val index = file.indexOf("/")
        if (index != -1) {
            val moduleName = file.substring(0, index)
            val module = moduleDescriptorMap[moduleName]
            if (module != null && addSpringExternalXmlFile(moduleDescriptorMap, relevantModule, getResourceDir(module), fileOnClasspath)) {
                return
            }
        }
        moduleDescriptorMap.values
            .any { addSpringExternalXmlFile(moduleDescriptorMap, relevantModule, getResourceDir(it), fileOnClasspath) }
    }

    private fun addSpringXmlFile(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        resourceDirectory: File,
        file: String
    ) = if (StringUtils.startsWith(file, "/")) {
        addSpringExternalXmlFile(moduleDescriptorMap, moduleDescriptor, getResourceDir(moduleDescriptor), file)
    } else addSpringExternalXmlFile(moduleDescriptorMap, moduleDescriptor, resourceDirectory, file)

    private fun getResourceDir(moduleToSearch: YModuleDescriptor) = File(
        moduleToSearch.moduleRootDirectory,
        HybrisConstants.RESOURCES_DIRECTORY
    )

    private fun addSpringExternalXmlFile(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        resourcesDir: File,
        file: String
    ) = File(resourcesDir, file)
        .takeIf { it.exists() }
        ?.let { processSpringFile(moduleDescriptorMap, moduleDescriptor, it) }
        ?: false

    companion object {
        private val LOG = Logger.getInstance(DefaultSpringConfigurator::class.java)
        private val SPLIT_PATTERN = Pattern.compile(" ,")
    }
}
