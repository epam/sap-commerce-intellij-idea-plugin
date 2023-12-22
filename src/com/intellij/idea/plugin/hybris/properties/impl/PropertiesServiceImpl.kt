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
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ADVANCED_PROPERTIES_FILE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.ENV_PROPERTIES_FILE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_OPT_CONFIG_DIR_ENV
import com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_RUNTIME_PROPERTIES_ENV
import com.intellij.idea.plugin.hybris.common.HybrisConstants.LOCAL_PROPERTIES_FILE
import com.intellij.idea.plugin.hybris.common.HybrisConstants.PROJECT_PROPERTIES_FILE
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


    override fun findProperty(key: String): String? = findAllProperties()
            .filter { it.key == key }
            .map { it.value }
            .firstOrNull()

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

        var propertiesFiles = ArrayList<PropertiesFile>();

        // Ignore Order and production.properties for now as `developer.mode` should be set to true for development anyway
        FileTypeIndex.getFiles(PropertiesFileType.INSTANCE, scope)
                .mapNotNull { PsiManager.getInstance(project).findFile(it) }
                .mapNotNull { it as? PropertiesFile }
                .forEach { file ->
                    when (file.name) {
                        ENV_PROPERTIES_FILE -> envPropsFile = file
                        ADVANCED_PROPERTIES_FILE -> advancedPropsFile = file
                        LOCAL_PROPERTIES_FILE -> localPropsFile = file
                        PROJECT_PROPERTIES_FILE -> propertiesFiles.add(file)
                    }
                }

        envPropsFile?.let { propertiesFiles.add(0, it) }
        advancedPropsFile?.let { propertiesFiles.add(1, it) }
        localPropsFile?.let { propertiesFiles.add(it) }

        propertiesFiles.forEach { file -> addPropertyFile(result, file) }

        loadHybrisRuntimeProperties(result)
        loadHybrisOptionalConfigDir(result)

        return ArrayList(result.values)
    }

    private fun findAllProperties(): LinkedHashMap<String, String> = findAllIProperties()
            .filter { it.value != null && it.key != null }
            .associateTo(LinkedHashMap()) { it.key!! to it.value!! }
            .let { properties ->
                properties.filter { it.value.contains(nestedPropertyPrefix) }
                        .forEach { replacePlaceholder(properties, it.key, HashSet<String>()) }
                return properties
            }

    private fun replacePlaceholder(result: LinkedHashMap<String, String>, key: String, visitedProperties: MutableSet<String>) {

        var lastIndex = 0

        val value = result[key] ?: ""
        var replacedValue = value

        while (true) {
            val startIndex = value.indexOf(nestedPropertyPrefix, lastIndex)
            val endIndex = value.indexOf(nestedPropertySuffix, startIndex + 1)
            lastIndex = endIndex + nestedPropertyPrefix.length

            if (startIndex == -1 || endIndex == -1)
                break

            val placeHolder = value.substring(startIndex, endIndex + nestedPropertySuffix.length)
            val nestedKey = placeHolder.substring(nestedPropertyPrefix.length, placeHolder.length - nestedPropertySuffix.length)
            if (visitedProperties.contains(nestedKey))
                continue
            visitedProperties.add(nestedKey)
            val nestedValue: String? = result[nestedKey]
            nestedValue?.let {
                var newValue = it
                if (it.contains(nestedPropertyPrefix)) {
                    replacePlaceholder(result, nestedKey, visitedProperties)
                    newValue = result[nestedKey] ?: ""
                }

                if (!newValue.contains(nestedPropertyPrefix)) {
                    replacedValue = replacedValue.replace(placeHolder, newValue)
                }
            }

        }
        result[key] = replacedValue;
    }

    private fun loadHybrisOptionalConfigDir(result: MutableMap<String, IProperty>) = (System.getenv(HYBRIS_OPT_CONFIG_DIR_ENV)
            ?: result[HybrisConstants.PROPERTY_OPTIONAL_CONFIG_DIR]?.value)
            ?.let { File(it) }
            ?.takeIf { it.isDirectory }
            ?.listFiles { _, name -> optionalPropertiesFilePattern.matcher(name).matches() }
            ?.associateByTo(TreeMap()) { it.name }
            ?.values
            ?.mapNotNull { toPropertiesFile(it) }
            ?.forEach { addPropertyFile(result, it) }

    private fun loadHybrisRuntimeProperties(result: MutableMap<String, IProperty>) = System.getenv(HYBRIS_RUNTIME_PROPERTIES_ENV)
            ?.takeIf { it.isNotBlank() }
            ?.let { File(it) }
            ?.let { toPropertiesFile(it) }
            ?.let { addPropertyFile(result, it) }

    private fun toPropertiesFile(file: File) = LocalFileSystem.getInstance().findFileByIoFile(file)
            ?.takeIf { it.exists() }
            ?.let { PsiManager.getInstance(project).findFile(it) }
            ?.let { it as? PropertiesFile }

    private fun addPropertyFile(result: MutableMap<String, IProperty>, propertiesFile: PropertiesFile?) {
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
        val envPropertiesScope = platformModule.moduleContentScope.filter { it.name == ENV_PROPERTIES_FILE }
        val advancedPropertiesScope = platformModule.moduleContentScope.filter { it.name == ADVANCED_PROPERTIES_FILE }
        val localPropertiesScope = configModule.moduleContentScope.filter { it.name == LOCAL_PROPERTIES_FILE }

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