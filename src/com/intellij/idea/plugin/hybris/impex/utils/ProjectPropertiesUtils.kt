package com.intellij.idea.plugin.hybris.impex.utils

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.OPTIONAL_CONFIG_DIR_KEY
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
import com.intellij.psi.search.GlobalSearchScope.everythingScope
import com.intellij.psi.search.GlobalSearchScope.getScopeRestrictedByFileTypes
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.set

object ProjectPropertiesUtils {
    private const val NESTED_PROPERTY_PREFIX = "\${"
    private const val NESTED_PROPERTY_SUFFIX = "}"
    private val OPTIONAL_PROPERTIES_FILE_PATTERN = Pattern.compile("([1-9]\\d)-(\\w*)\\.properties")

    fun findAllProperties(project: Project): List<IProperty> {
        val result = LinkedHashMap<String, IProperty>()
        val configModule = obtainConfigModule(project)
        val platformModule = obtainPlatformModule(project)
        val scope = createSearchScope(project, configModule, platformModule)
        val files = FileTypeIndex.getFiles(PropertiesFileType.INSTANCE, scope)
        var advancedPropsFile: PropertiesFile? = null
        var localPropsFile: PropertiesFile? = null
        for (virtualFile in files) {
            val file = PsiManager.getInstance(project).findFile(virtualFile) as PropertiesFile?
            if (file != null) {
                if (file.name == "advanced.properties") {
                    advancedPropsFile = file
                } else if (file.name == "local.properties") {
                    localPropsFile = file
                } else {
                    for (property in file.properties) {
                        if (property.key != null) {
                            result[property.key!!] = property
                        }
                    }
                }
            }
        }
        addPropertyFile(result, advancedPropsFile)
        addPropertyFile(result, localPropsFile)

        val optDir = result[OPTIONAL_CONFIG_DIR_KEY]
        addOptionalConfiguration(project, result, optDir)

        return ArrayList(result.values)
    }

    fun findAutoCompleteProperties(project: Project, query: String): List<IProperty> =
        findAllProperties(project).filter { it.key != null && it.key!!.contains(query) || query.isBlank()}

    fun findMacroProperty(project: Project, query: String): IProperty? {
        val allProps = findAllProperties(project)
        if (allProps.isEmpty()) {
            return null;
        }
        val filteredProps = allProps.filter { it.key != null && query.contains(it.key!!) || query.isBlank() }
        if (filteredProps.isEmpty()) {
            return null;
        }

        return filteredProps.reduce { one, two -> if (one.key!!.length > two.key!!.length) one else two }
    }

    fun resolvePropertyValue(project: Project, value: String?): String {
        return resolvePropertyValue(project, value, HashMap())
    }

    private fun resolvePropertyValue(project: Project, value: String?, resolvedProperties: MutableMap<String, String>): String {
        if (value == null) {
            return ""
        }
        var index = 0
        val sb = StringBuilder()
        while (index != -1) {
            val startIndex = value.indexOf(NESTED_PROPERTY_PREFIX, index)
            val endIndex = value.indexOf(NESTED_PROPERTY_SUFFIX, startIndex)
            if (startIndex != -1 && endIndex != -1) {
                sb.append(value, index, startIndex)
                val propertyKey = value.substring(startIndex + NESTED_PROPERTY_PREFIX.length, endIndex)
                var resolvedValue: String? = resolvedProperties[propertyKey]
                if (resolvedValue != null) {
                    sb.append(resolvedValue)
                } else {
                    val property = findMacroProperty(project, propertyKey)
                    if (property != null) {
                        resolvedValue = resolvePropertyValue(project, property.value)
                        sb.append(resolvedValue)
                        resolvedProperties[propertyKey] = resolvedValue
                    } else {
                        sb.append(NESTED_PROPERTY_PREFIX).append(propertyKey).append(NESTED_PROPERTY_SUFFIX)
                    }
                }
                index = endIndex + 1
            } else {
                sb.append(value, index, value.length)
                index = startIndex
            }
        }
        return sb.toString()
    }

    private fun addOptionalConfiguration(project: Project, result: java.util.LinkedHashMap<String, IProperty>, optDir: IProperty?) {
        if (optDir == null) {
            return
        }
        val dir = File(optDir.value)
        if (!dir.isDirectory) {
            return
        }
        val matchedFiles = dir.listFiles { _, name -> OPTIONAL_PROPERTIES_FILE_PATTERN.matcher(name).matches() }
            ?: return
        val propertyFiles = TreeMap<String, File>()
        Arrays.stream(matchedFiles).forEach { file -> propertyFiles[file.name] = file }

        propertyFiles.values.forEach { file ->
            val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
            if (virtualFile == null || !virtualFile.exists()) {
                return
            }
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile is PropertiesFile) {
                addPropertyFile(result, psiFile as PropertiesFile?)
            }
        }
    }

    private fun addPropertyFile(result: java.util.LinkedHashMap<String, IProperty>, propertiesFile: PropertiesFile?) {
        if (propertiesFile == null) {
            return
        }
        for (property in propertiesFile.properties) {
            if (property.key != null) {
                result[property.key!!] = property
            }
        }
    }

    private fun createSearchScope(project: Project, configModule: Module, platformModule: Module): GlobalSearchScope {
        val projectPropertiesScope = getScopeRestrictedByFileTypes(everythingScope(project), PropertiesFileType.INSTANCE)
            .filter { it.name == "project.properties" }
        val advancedPropertiesScope = platformModule.moduleContentScope.filter { it.name == "advanced.properties" }
        val localPropertiesScope = configModule.moduleContentScope.filter { it.name == "local.properties" }

        return projectPropertiesScope.or(advancedPropertiesScope).or(localPropertiesScope)
    }

    private fun obtainConfigModule(project: Project) =
        ModuleManager.getInstance(project).modules.first { it.name == HybrisConstants.EXTENSION_NAME_CONFIG }

    private fun obtainPlatformModule(project: Project) =
        ModuleManager.getInstance(project).modules.first { it.name == HybrisConstants.EXTENSION_NAME_PLATFORM }

    fun GlobalSearchScope.filter(filter: (VirtualFile) -> Boolean ) = object : DelegatingGlobalSearchScope(this) {
        override fun contains(file: VirtualFile): Boolean {
            return filter(file) && super.contains(file)
        }
    }

    fun GlobalSearchScope.or(otherScope: GlobalSearchScope): GlobalSearchScope = union(otherScope)

}