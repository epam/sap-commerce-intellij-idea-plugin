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

package sap.commerce.toolset.project

import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.concurrency.AppExecutorUtil
import sap.commerce.toolset.HybrisConstants
import java.io.File
import java.util.*
import java.util.regex.Pattern

/**
 * Currently there is an issue with Order and Properties that are included in lookup and suggestion.
 *
 * @see <a href="https://help.sap.com/docs/SAP_COMMERCE/b490bb4e85bc42a7aa09d513d0bcb18e/8b8e13c9866910149d40b151a9196543.html?locale=en-US">Configuring the Behavior of SAP Commerce</a> to improve order of the properties.
 * @see <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/d090fb3dd48a418d967a1dfdca9fcac6.html?locale=en-US">SAP Commerce Cloud Properties</a> to support CCv2 properties.
 */
@Service(Service.Level.PROJECT)
class PropertyService(private val project: Project) {

    private val nestedPropertyPrefix = "\${"
    private val nestedPropertySuffix = "}"
    private val optionalPropertiesFilePattern = Pattern.compile("([1-9]\\d)-(\\w*)\\.properties")

    private val cachedProperties = CachedValuesManager.getManager(project).createCachedValue(
        {
            val result = LinkedHashMap<String, IProperty>()
            val configModule = obtainConfigModule() ?: return@createCachedValue CachedValueProvider.Result.create(emptyList(), ModificationTracker.NEVER_CHANGED)
            val platformModule = obtainPlatformModule() ?: return@createCachedValue CachedValueProvider.Result.create(emptyList(), ModificationTracker.NEVER_CHANGED)
            val scope = createSearchScope(configModule, platformModule)
            var envPropsFile: PropertiesFile? = null
            var advancedPropsFile: PropertiesFile? = null
            var localPropsFile: PropertiesFile? = null

            val propertiesFiles = ArrayList<PropertiesFile>()

            // Ignore Order and production.properties for now as `developer.mode` should be set to true for development anyway
            FileTypeIndex.getFiles(PropertiesFileType.INSTANCE, scope)
                .mapNotNull { PsiManager.getInstance(project).findFile(it) }
                .mapNotNull { it as? PropertiesFile }
                .forEach { file ->
                    when (file.name) {
                        HybrisConstants.ENV_PROPERTIES_FILE -> envPropsFile = file
                        HybrisConstants.ADVANCED_PROPERTIES_FILE -> advancedPropsFile = file
                        HybrisConstants.LOCAL_PROPERTIES_FILE -> localPropsFile = file
                        HybrisConstants.PROJECT_PROPERTIES_FILE -> propertiesFiles.add(file)
                        HybrisConstants.PLATFORMHOME_PROPERTIES_FILE -> propertiesFiles.add(file)
                    }
                }

            localPropsFile?.let { propertiesFiles.add(it) }
            advancedPropsFile?.let { propertiesFiles.add(0, it) }
            envPropsFile?.let { propertiesFiles.add(0, it) }

            propertiesFiles.forEach { addPropertyFile(result, it) }

            loadHybrisRuntimeProperties(result)
            loadHybrisOptionalConfigDir(result)

            CachedValueProvider.Result.create(
                result.values.toList(), propertiesFiles
                    .map { it.virtualFile }
                    .toTypedArray()
                    .ifEmpty { ModificationTracker.EVER_CHANGED }
            )
        }, false
    )

    fun getLanguages(): Set<String> {
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

    fun containsLanguage(language: String, supportedLanguages: Set<String>) = supportedLanguages
        .contains(language.lowercase())

    fun findProperty(query: String): String? = findAllProperties()[query]

    fun findAutoCompleteProperties(query: String): List<IProperty> = application.runReadAction<List<IProperty>> {
        findAllIProperties()
            .filter { it.key != null && it.key!!.contains(query) || query.isBlank() }
    }

    fun findMacroProperty(query: String): IProperty? = application.runReadAction<IProperty?> {
        findAllIProperties()
            .takeIf { it.isNotEmpty() }
            ?.filter { it.key != null && query.contains(it.key!!) || query.isBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?.reduce { one, two -> if (one.key!!.length > two.key!!.length) one else two }
    }

    fun findAllProperties(): Map<String, String> = application.runReadAction<Map<String, String>> {
        findAllIProperties()
            .filter { it.value != null && it.key != null }
            .associateTo(LinkedHashMap()) { it.key!! to it.value!! }
            .let { properties ->
                addEnvironmentProperties(properties)
                properties
                    .filter { it.value.contains(nestedPropertyPrefix) }
                    .forEach { replacePlaceholder(properties, it.key, mutableSetOf()) }
                return@let properties
            }
    }

    fun initCache() = ReadAction
        .nonBlocking<Collection<IProperty>> {
            findAllIProperties()
        }
        .inSmartMode(project)
        .submit(AppExecutorUtil.getAppExecutorService())

    fun getPlatformHome(): String? = findPlatformRootDirectory(project)
        ?.path

    private fun findAllIProperties() = cachedProperties.value

    private fun addEnvironmentProperties(properties: MutableMap<String, String>) {
        val platformHomePropertyKey = HybrisConstants.PROPERTY_PLATFORMHOME
        getPlatformHome()
            ?.let { properties[platformHomePropertyKey] = it }

        properties[HybrisConstants.PROPERTY_ENV_PROPERTY_PREFIX]
            ?.let { prefix ->
                System.getenv()
                    .filter { it.key.startsWith(prefix) }
                    .forEach {
                        val envPropertyKey = it.key.substring(prefix.length)
                        val key = envPropertyKey.replace("__", "##")
                            .replace("_", ".")
                            .replace("##", "_")
                        properties[key] = it.value
                    }
            }
    }

    private fun replacePlaceholder(result: MutableMap<String, String>, key: String, visitedProperties: MutableSet<String>): String {
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
        result[key] = replacedValue
        return replacedValue
    }

    private fun loadHybrisOptionalConfigDir(result: MutableMap<String, IProperty>) = (System.getenv(HybrisConstants.ENV_HYBRIS_OPT_CONFIG_DIR)
        ?: result[HybrisConstants.PROPERTY_OPTIONAL_CONFIG_DIR]?.value)
        ?.let { property ->
            if (property.contains(nestedPropertyPrefix)) {
                val properties = result
                    .mapValues { it.value.value ?: "" }
                    .toMutableMap()
                properties["_____temp_property_opt_config_dir"] = property
                replacePlaceholder(properties, "_____temp_property_opt_config_dir", mutableSetOf())
            } else property
        }
        ?.let { File(it) }
        ?.takeIf { it.isDirectory }
        ?.listFiles { _, name -> optionalPropertiesFilePattern.matcher(name).matches() }
        ?.associateByTo(TreeMap()) { it.name }
        ?.values
        ?.mapNotNull { toPropertiesFile(it) }
        ?.forEach { addPropertyFile(result, it) }

    private fun loadHybrisRuntimeProperties(result: MutableMap<String, IProperty>) = System.getenv(HybrisConstants.ENV_HYBRIS_RUNTIME_PROPERTIES)
        ?.takeIf { it.isNotBlank() }
        ?.let { File(it) }
        ?.let { toPropertiesFile(it) }
        ?.let { addPropertyFile(result, it) }

    private fun toPropertiesFile(file: File) = LocalFileSystem.getInstance().findFileByIoFile(file)
        ?.takeIf { it.exists() }
        ?.let { PsiManager.getInstance(project).findFile(it) }
        ?.asSafely<PropertiesFile>()

    private fun addPropertyFile(result: MutableMap<String, IProperty>, propertiesFile: PropertiesFile) {
        for (property in propertiesFile.properties) {
            if (property.key != null) {
                result[property.key!!] = property
            }
        }
    }

    private fun createSearchScope(configModule: Module, platformModule: Module): GlobalSearchScope {
        val projectPropertiesScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.everythingScope(project), PropertiesFileType.INSTANCE)
            .filter { it.name == HybrisConstants.PROJECT_PROPERTIES_FILE || it.name == HybrisConstants.PLATFORMHOME_PROPERTIES_FILE}
        val envPropertiesScope = platformModule.moduleContentScope.filter { it.name == HybrisConstants.ENV_PROPERTIES_FILE }
        val advancedPropertiesScope = platformModule.moduleContentScope.filter { it.name == HybrisConstants.ADVANCED_PROPERTIES_FILE }
        val localPropertiesScope = configModule.moduleContentScope.filter { it.name == HybrisConstants.LOCAL_PROPERTIES_FILE }

        return envPropertiesScope
            .or(advancedPropertiesScope)
            .or(localPropertiesScope)
            .or(projectPropertiesScope)
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

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PropertyService = project.service()
    }
}