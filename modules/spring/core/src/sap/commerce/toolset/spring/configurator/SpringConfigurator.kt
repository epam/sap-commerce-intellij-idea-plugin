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

package sap.commerce.toolset.spring.configurator

import com.intellij.facet.ModifiableFacetModel
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.spring.facet.SpringFacet
import com.intellij.spring.settings.SpringGeneralSettings
import org.apache.commons.lang3.StringUtils
import org.jdom.Element
import org.jdom.JDOMException
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectPreImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectStartupConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.YRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCoreExtModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.project.yExtensionName
import sap.commerce.toolset.util.fileExists
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern
import java.util.zip.ZipFile
import kotlin.io.path.*

class SpringConfigurator : ProjectPreImportConfigurator, ProjectImportConfigurator, ProjectStartupConfigurator {

    private val patternSplitByComma = Pattern.compile(" ,")

    override val name: String
        get() = "Spring"

    override fun preConfigure(importContext: ProjectImportContext) {
        if (Plugin.SPRING.isDisabled()) return

        val moduleDescriptors = importContext.chosenHybrisModuleDescriptors
            .filterIsInstance<YModuleDescriptor>()
            .associateBy { it.name }

        for (moduleDescriptor in moduleDescriptors.values) {
            try {
                when (moduleDescriptor) {
                    is YWebSubModuleDescriptor -> process(moduleDescriptors, moduleDescriptor)
                    is YRegularModuleDescriptor -> process(moduleDescriptors, moduleDescriptor)
                }
            } catch (e: Exception) {
                thisLogger().error("Unable to parse Spring context for module " + moduleDescriptor.name, e)
            }
        }

        moduleDescriptors.values
            .filterIsInstance<YCoreExtModuleDescriptor>()
            .forEach { moduleDescriptor ->
                val advancedProperties = importContext.platformModuleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.ADVANCED_PROPERTIES)
                moduleDescriptor.addSpringFile(advancedProperties.pathString)

                val configModuleDescriptor = importContext.configModuleDescriptor
                configModuleDescriptor.moduleRootPath.resolve(ProjectConstants.File.LOCAL_PROPERTIES)
                    .takeIf { it.fileExists }
                    ?.let { moduleDescriptor.addSpringFile(it.pathString) }
            }
    }

    override fun configure(
        importContext: ProjectImportContext,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        if (Plugin.SPRING.isDisabled()) return

        val moduleDescriptors = importContext.chosenHybrisModuleDescriptors
            .associateBy { it.name }
        val facetModels = modifiableModelsProvider.modules
            .associate { it.yExtensionName() to modifiableModelsProvider.getModifiableFacetModel(it) }

        moduleDescriptors.values
            .forEach { configureFacetDependencies(it, facetModels, it.getDirectDependencies()) }
    }

    override fun onStartup(project: Project) {
        Plugin.SPRING.ifActive {
            with(SpringGeneralSettings.getInstance(project)) {
                isShowMultipleContextsPanel = false
                isShowProfilesPanel = false
            }
        }
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
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YRegularModuleDescriptor
    ) {
        val projectProperties = Properties()
        val propFile = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.File.PROJECT_PROPERTIES)
        moduleDescriptor.addSpringFile(propFile.pathString)
        try {
            projectProperties.load(propFile.inputStream())
        } catch (_: FileNotFoundException) {
            return
        } catch (e: IOException) {
            thisLogger().error("", e)
            return
        }

        // specifci case for OCC like extensions, usually, they have web-spring.xml files in the corresponding resources folder
        projectProperties.getProperty("ext.${moduleDescriptor.name}.extension.webmodule.webroot")
            ?.let { if (it.startsWith("/")) it.removePrefix("/") else it }
            ?.let {
                moduleDescriptor.resourcesPath
                    .resolve(it)
                    .resolve(moduleDescriptor.name)
                    .resolve("web")
                    .resolve("spring")
            }
            ?.takeIf { it.exists() }
            ?.let { addSpringXmlFile(moduleDescriptorMap, moduleDescriptor, it, moduleDescriptor.name + "-web-spring.xml") }

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
                            .filterNot { addSpringXmlFile(moduleDescriptorMap, relevantModule, relevantModule.resourcesPath, it) }
                            .forEach { fileName ->
                                val dir = hackGuessLocation(relevantModule)
                                if (!addSpringXmlFile(moduleDescriptorMap, relevantModule, dir, fileName)) {
                                    // otherwise we can scan in all other extensions, HybrisContextFactory does the same in the getResource() methods
                                    // it is the case for `common` extension which has `common-spring.xml` in the `platformservices` extension

                                    moduleDescriptorMap.entries
                                        .filter { it.key != moduleName }
                                        .firstOrNull {
                                            val anotherModule = it.value
                                            addSpringXmlFile(moduleDescriptorMap, anotherModule, anotherModule.resourcesPath, fileName)
                                        }
                                }
                            }
                    }
            }

        if (moduleDescriptor.extensionInfo.backofficeModule) {
            moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES).listDirectoryEntries()
                .filter { it.name.endsWith("-backoffice-spring.xml") }
                .forEach { processSpringFile(moduleDescriptorMap, moduleDescriptor, it) }
        }
    }

    // This is not a nice practice but the platform has a bug in acceleratorstorefrontcommons/project.properties.
    // See https://jira.hybris.com/browse/ECP-3167
    private fun hackGuessLocation(moduleDescriptor: YModuleDescriptor) = moduleDescriptor.resourcesPath
        .resolve(moduleDescriptor.name)
        .resolve("web")
        .resolve("spring")

    @Throws(IOException::class, JDOMException::class)
    private fun process(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YWebSubModuleDescriptor
    ) {
        moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_WEB_XML)
            .takeIf { it.fileExists }
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
        moduleDescriptor: YWebSubModuleDescriptor,
        contextConfigLocation: String
    ) {
        val webModuleDir = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Directory.WEB_ROOT)

        patternSplitByComma.split(contextConfigLocation)
            .filter { it.endsWith(".xml") }
            .map { webModuleDir.resolve(it) }
            .filter { it.fileExists }
            .forEach { processSpringFile(moduleDescriptorMap, moduleDescriptor, it) }

        // In addition to plain xml files also scan jars in the WEB-INF/lib
        val webInfLibDir = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_LIB)
        VfsUtil.findFile(webInfLibDir, true)
            ?.children
            ?.filter { it.extension == "jar" }
            ?.forEach {
                val file = VfsUtil.virtualToIoFile(it)
                val zipFile = ZipFile(file)
                val entries = zipFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    if (name.startsWith("META-INF") && name.endsWith(".xml")) {
                        zipFile.getInputStream(entry).use { inputStream ->
                            val element = JDOMUtil.load(inputStream)
                            if (!element.isEmpty && element.name == "beans") {
                                // as for now, imports are not scanned
                                val springFile = "jar://${file.absolutePath}!/$name"
                                moduleDescriptor.addSpringFile(springFile)
                            }
                        }
                    }
                }
            }
    }

    @Throws(IOException::class, JDOMException::class)
    private fun getDocumentRoot(inputFile: Path) = JDOMUtil.load(inputFile)

    @Throws(IOException::class, JDOMException::class)
    private fun hasSpringContent(springFile: Path) = with(getDocumentRoot(springFile)) {
        !this.isEmpty && this.name == "beans"
    }

    private fun processSpringFile(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        relevantModule: YModuleDescriptor,
        springFile: Path
    ): Boolean {
        try {
            if (!hasSpringContent(springFile)) return false

            if (relevantModule.addSpringFile(springFile.pathString)) {
                scanForSpringImport(moduleDescriptorMap, relevantModule, springFile)
            }
            return true
        } catch (e: Exception) {
            thisLogger().error("unable scan file for spring imports " + springFile.name)
        }
        return false
    }

    @Throws(IOException::class, JDOMException::class)
    private fun scanForSpringImport(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        springFile: Path
    ) {
        getDocumentRoot(springFile).children
            .filter { it.name == "import" }
            .forEach { processImportNodeList(moduleDescriptorMap, moduleDescriptor, it, springFile) }
    }

    private fun processImportNodeList(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        import: Element,
        springFile: Path
    ) {
        val resource = import.getAttributeValue("resource")

        if (resource.startsWith("classpath:")) {
            addSpringOnClasspath(moduleDescriptorMap, moduleDescriptor, resource.substring("classpath:".length))
        } else {
            addSpringXmlFile(moduleDescriptorMap, moduleDescriptor, springFile.parent, resource)
        }
    }

    private fun addSpringOnClasspath(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        relevantModule: YModuleDescriptor,
        fileOnClasspath: String
    ) {
        val resourceDirectory = relevantModule.resourcesPath
        if (addSpringXmlFile(moduleDescriptorMap, relevantModule, resourceDirectory, fileOnClasspath)) return

        val file = StringUtils.stripStart(fileOnClasspath, "/")

        val index = file.indexOf("/")
        if (index != -1) {
            val moduleName = file.substring(0, index)
            val module = moduleDescriptorMap[moduleName]
            if (module != null && addSpringExternalXmlFile(moduleDescriptorMap, relevantModule, module.resourcesPath, fileOnClasspath)) {
                return
            }
        }
        moduleDescriptorMap.values
            .any { addSpringExternalXmlFile(moduleDescriptorMap, relevantModule, it.resourcesPath, fileOnClasspath) }
    }

    private fun addSpringXmlFile(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        resourceDirectory: Path,
        fileName: String
    ) = if (fileName.startsWith("/")) addSpringExternalXmlFile(moduleDescriptorMap, moduleDescriptor, moduleDescriptor.resourcesPath, fileName)
    else addSpringExternalXmlFile(moduleDescriptorMap, moduleDescriptor, resourceDirectory, fileName)

    private val YModuleDescriptor.resourcesPath
        get() = moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES)

    private fun addSpringExternalXmlFile(
        moduleDescriptorMap: Map<String, YModuleDescriptor>,
        moduleDescriptor: YModuleDescriptor,
        resourcesDir: Path,
        fileName: String
    ) = resourcesDir.resolve(fileName)
        .takeIf { it.fileExists }
        ?.let { processSpringFile(moduleDescriptorMap, moduleDescriptor, it) }
        ?: false

}