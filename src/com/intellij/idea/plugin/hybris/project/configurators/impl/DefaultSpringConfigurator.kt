/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCoreExtModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YRegularModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YWebSubModuleDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.spring.facet.SpringFacet
import org.apache.commons.lang3.StringUtils
import org.jdom.Element
import org.jdom.JDOMException
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class DefaultSpringConfigurator : SpringConfigurator {

    override fun processSpringConfiguration(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: Map<String, ModuleDescriptor>
    ) {
        for (moduleDescriptor in moduleDescriptors.values) {
            try {
                when (moduleDescriptor) {
                    is YWebSubModuleDescriptor -> process(moduleDescriptors, moduleDescriptor)
                    is YRegularModuleDescriptor -> process(moduleDescriptors, moduleDescriptor)
                }
            } catch (e: Exception) {
                LOG.error("Unable to parse Spring context for module " + moduleDescriptor.name, e)
            }
        }

        moduleDescriptors.values.firstIsInstanceOrNull<YCoreExtModuleDescriptor>()
            ?.let { moduleDescriptor ->
                val advancedProperties = File(hybrisProjectDescriptor.platformHybrisModuleDescriptor.moduleRootDirectory, HybrisConstants.ADVANCED_PROPERTIES)
                moduleDescriptor.addSpringFile(advancedProperties.absolutePath)

                hybrisProjectDescriptor.configHybrisModuleDescriptor
                    ?.let { File(it.moduleRootDirectory, HybrisConstants.LOCAL_PROPERTIES) }
                    ?.let { moduleDescriptor.addSpringFile(it.absolutePath) }
            }

    }

    override fun configureDependencies(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: MutableMap<String, ModuleDescriptor>,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val facetModels = modifiableModelsProvider.modules
            .associate { it.yExtensionName() to modifiableModelsProvider.getModifiableFacetModel(it) }

        moduleDescriptors.values
            .forEach { configureFacetDependencies(it, facetModels, it.getDirectDependencies()) }
    }

    private fun configureFacetDependencies(
        moduleDescriptor: ModuleDescriptor,
        facetModels: Map<String, ModifiableFacetModel>,
        dependencies: Set<ModuleDescriptor>
    ) {
        val springFileSet = getSpringFileSet(facetModels, moduleDescriptor)
            ?: return

        dependencies
            .sorted()
            .mapNotNull { getSpringFileSet(facetModels, it) }
            .forEach { springFileSet.addDependency(it) }
    }

    private fun getSpringFileSet(
        facetModels: Map<String, ModifiableFacetModel>,
        moduleDescriptor: ModuleDescriptor
    ) = facetModels[moduleDescriptor.name]
        ?.getFacetByType(SpringFacet.FACET_TYPE_ID)
        ?.fileSets
        ?.takeIf { it.isNotEmpty() }
        ?.iterator()
        ?.next()

    private fun process(
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
        moduleDescriptor: YRegularModuleDescriptor
    ) {
        val projectProperties = Properties()
        val propFile = File(moduleDescriptor.moduleRootDirectory, HybrisConstants.PROJECT_PROPERTIES)
        moduleDescriptor.addSpringFile(propFile.absolutePath)
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

        if (moduleDescriptor.hasBackofficeModule) {
            File(moduleDescriptor.moduleRootDirectory, HybrisConstants.RESOURCES_DIRECTORY)
                .listFiles { _, name: String -> name.endsWith("-backoffice-spring.xml") }
                ?.forEach { processSpringFile(moduleDescriptorMap, moduleDescriptor, it) }
        }
    }

    // This is not a nice practice but the platform has a bug in acceleratorstorefrontcommons/project.properties.
    // See https://jira.hybris.com/browse/ECP-3167
    private fun hackGuessLocation(moduleDescriptor: ModuleDescriptor) = File(
        getResourceDir(moduleDescriptor),
        FileUtilRt.toSystemDependentName(moduleDescriptor.name + "/web/spring")
    )

    @Throws(IOException::class, JDOMException::class)
    private fun process(
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
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
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
        moduleDescriptor: ModuleDescriptor,
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
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
        relevantModule: ModuleDescriptor,
        springFile: File
    ): Boolean {
        try {
            if (!hasSpringContent(springFile)) return false

            if (relevantModule.addSpringFile(springFile.absolutePath)) {
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
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
        moduleDescriptor: ModuleDescriptor,
        springFile: File
    ) {
        getDocumentRoot(springFile).children
            .filter { it.name == "import" }
            .forEach { processImportNodeList(moduleDescriptorMap, moduleDescriptor, it, springFile) }
    }

    private fun processImportNodeList(
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
        moduleDescriptor: ModuleDescriptor,
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
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
        relevantModule: ModuleDescriptor,
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
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
        moduleDescriptor: ModuleDescriptor,
        resourceDirectory: File,
        file: String
    ) = if (StringUtils.startsWith(file, "/")) {
        addSpringExternalXmlFile(moduleDescriptorMap, moduleDescriptor, getResourceDir(moduleDescriptor), file)
    } else addSpringExternalXmlFile(moduleDescriptorMap, moduleDescriptor, resourceDirectory, file)

    private fun getResourceDir(moduleToSearch: ModuleDescriptor) = File(
        moduleToSearch.moduleRootDirectory,
        HybrisConstants.RESOURCES_DIRECTORY
    )

    private fun addSpringExternalXmlFile(
        moduleDescriptorMap: Map<String, ModuleDescriptor>,
        moduleDescriptor: ModuleDescriptor,
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
