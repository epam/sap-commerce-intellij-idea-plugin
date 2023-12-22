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

package com.intellij.idea.plugin.hybris.properties.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.properties.PropertiesService
import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.File
import java.util.*
import java.util.regex.Pattern

/*
Improve order of the properties - https://help.sap.com/docs/SAP_COMMERCE/b490bb4e85bc42a7aa09d513d0bcb18e/8b8e13c9866910149d40b151a9196543.html?locale=en-US
 */
class PropertiesServiceImpl(val project: Project) : PropertiesService {

    private val nestedPropertyPrefix = "\${"
    private val nestedPropertySuffix = "}"
    private val optionalPropertiesFilePattern = Pattern.compile("([1-9]\\d)-(\\w*)\\.properties")

    override fun getLanguages(): Set<String> {
        val languages = findProperty(HybrisConstants.PROPERTY_LANG_PACKS)
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()

        val uniqueLanguages = languages.toMutableSet()
        uniqueLanguages.add(HybrisConstants.DEFAULT_LANGUAGE_ISOCODE)

        return uniqueLanguages
            .map { it.lowercase() }
            .toSet()
    }

    override fun containsLanguage(language: String, supportedLanguages: Set<String>) = supportedLanguages
        .contains(language.lowercase())


    override fun findProperty(key: String): String? = findAllProperties().filter { it.key == key }.map { it.value }.firstOrNull()

    override fun findAutoCompleteProperties(query: String): List<IProperty> = findAllIProperties()
        .filter { it.key != null && it.key!!.contains(query) || query.isBlank() }

    override fun findMacroProperty(query: String): IProperty? {
        val allProps = findAllIProperties()
        if (allProps.isEmpty()) {
            return null;
        }
        val filteredProps = allProps.filter { it.key != null && query.contains(it.key!!) || query.isBlank() }
        if (filteredProps.isEmpty()) {
            return null;
        }

        return filteredProps.reduce { one, two -> if (one.key!!.length > two.key!!.length) one else two }
    }


    private fun findAllIProperties(): List<IProperty> {
        val result = LinkedHashMap<String, IProperty>()
        val configModule = obtainConfigModule() ?: return emptyList()
        val platformModule = obtainPlatformModule() ?: return emptyList()
        val scope = createSearchScope(configModule, platformModule)
        var envPropsFile: PropertiesFile? = null
        var advancedPropsFile: PropertiesFile? = null
        var localPropsFile: PropertiesFile? = null

        var propertieFiles = ArrayList<PropertiesFile>();

        // Ignore Order and production.properties for now as `developer.mode` should be set to true for development anyway
        FileTypeIndex.getFiles(PropertiesFileType.INSTANCE, scope)
            .mapNotNull { PsiManager.getInstance(project).findFile(it) }
            .mapNotNull { it as? PropertiesFile }
            .forEach { file ->
                when (file.name) {
                    "env.properties" -> envPropsFile = file
                    "advanced.properties" -> advancedPropsFile = file
                    "local.properties" -> localPropsFile = file
                    "project.properties" -> propertieFiles.add(file)
                }
            }

        envPropsFile?.let { propertieFiles.add(0, it) }
        advancedPropsFile?.let { propertieFiles.add(1, it) }
        localPropsFile?.let { propertieFiles.add(it) }

        propertieFiles.forEach { file -> addPropertyFile(result, file) }

        loadHybrisRuntimeProperties(result)
        loadHybrisOptionalConfigDir(result)

        return ArrayList(result.values)
    }

    private fun findAllProperties(): LinkedHashMap<String, String> {

        val result = LinkedHashMap<String, String>()
        findAllIProperties().forEach { property ->
            property.value?.let {
                result[property.key!!] = it
            }
        }

        replacePlaceholders(result)

        return result
    }

    private fun replacePlaceholders(result: LinkedHashMap<String, String>) {
        result.forEach { key, value ->
            if (value.contains(nestedPropertyPrefix)) {
                replacePlaceholder(result, key, value);
            }
        }
    }

    private fun replacePlaceholder(result: LinkedHashMap<String, String>, key: String, value: String) {
        val pattern = Pattern.compile("\\$\\{[^\\}]*\\}")
        val matcher = pattern.matcher(value)
        matcher?.let {
            while (it.find()) {
                val placeholder: String = it.group()
                val nestedKey: String = placeholder.substring(nestedPropertyPrefix.length, placeholder.length - nestedPropertySuffix.length)
                val nestedValue: String? = result[nestedKey]
                nestedValue?.let {
                    var newValue = nestedValue
                    if (nestedValue.contains(nestedPropertyPrefix)) {
                        replacePlaceholder(result, nestedKey, nestedValue)
                        newValue = result[nestedKey]
                    }

                    result[key] = value.replace(placeholder, newValue ?: "")
                }
            }
        }
    }

    private fun loadHybrisOptionalConfigDir(result: LinkedHashMap<String, IProperty>) {
        var optDir = result[HybrisConstants.PROPERTY_OPTIONAL_CONFIG_DIR]?.value

        val envOptConfigDir = System.getenv("HYBRIS_OPT_CONFIG_DIR")
        if (envOptConfigDir != null) {
            optDir = envOptConfigDir;
        }

        optDir?.let {
            val dir = File(it)
            if (!dir.isDirectory) {
                return
            }

            val matchedFiles = dir.listFiles { _, name -> optionalPropertiesFilePattern.matcher(name).matches() }
                ?: return
            val propertyFiles = TreeMap<String, File>()
            Arrays.stream(matchedFiles).forEach { file -> propertyFiles[file.name] = file }

            propertyFiles.values.forEach { file ->
                addPropertyFile(result, toPropertiesFile(file))
            }
        }
    }

    private fun loadHybrisRuntimeProperties(result: LinkedHashMap<String, IProperty>) {
        val envVar: String = System.getenv("HYBRIS_RUNTIME_PROPERTIES") ?: ""
        if (!envVar.isBlank()) {
            var propertiesFile = File(envVar);
            propertiesFile?.let { file: File ->
                addPropertyFile(result, toPropertiesFile(file))
            }

        }
    }

    private fun toPropertiesFile(file: File): PropertiesFile? {
        val virtualFile: VirtualFile? = LocalFileSystem.getInstance().findFileByIoFile(file)
        if (virtualFile == null || !virtualFile.exists()) {
            return null
        }
        val psiFile: PsiFile? = PsiManager.getInstance(project).findFile(virtualFile)
        if (psiFile is PropertiesFile) {
            return psiFile
        }
        return null
    }


    private fun addPropertyFile(result: LinkedHashMap<String, IProperty>, propertiesFile: PropertiesFile?) {
        if (propertiesFile == null) {
            return
        }
        for (property in propertiesFile.properties) {
            if (property.key != null) {
                result[property.key!!] = property
            }
        }
    }

    private fun createSearchScope(configModule: Module, platformModule: Module): GlobalSearchScope {
        val projectPropertiesScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.everythingScope(project), PropertiesFileType.INSTANCE)
            .filter { it.name == "project.properties" }
        val envPropertiesScope = platformModule.moduleContentScope.filter { it.name == "env.properties" }
        val advancedPropertiesScope = platformModule.moduleContentScope.filter { it.name == "advanced.properties" }
        val localPropertiesScope = configModule.moduleContentScope.filter { it.name == "local.properties" }

        return projectPropertiesScope.or(envPropertiesScope.or(advancedPropertiesScope.or(localPropertiesScope)))
    }

    private fun obtainConfigModule() = ModuleManager.getInstance(project)
        .modules
        .firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_CONFIG }

    private fun obtainPlatformModule() = ModuleManager.getInstance(project)
        .modules
        .firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_PLATFORM }

    fun GlobalSearchScope.filter(filter: (VirtualFile) -> Boolean) = object : DelegatingGlobalSearchScope(this) {
        override fun contains(file: VirtualFile): Boolean {
            return filter(file) && super.contains(file)
        }
    }

    fun GlobalSearchScope.or(otherScope: GlobalSearchScope): GlobalSearchScope = union(otherScope)

}