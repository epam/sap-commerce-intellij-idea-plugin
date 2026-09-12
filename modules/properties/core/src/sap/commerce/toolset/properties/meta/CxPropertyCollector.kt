/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.meta

import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.removeUserData
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.application
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.project.yExtensionName
import sap.commerce.toolset.project.yModule
import java.io.File
import java.util.*
import java.util.regex.Pattern

/**
 * Collects every local declaration of every SAP Commerce property into a [CxPropertyModel], preserving the file each
 * value comes from instead of flattening the chain into a single map.
 *
 * The chain is walked in three passes, because each one needs the previous one to be resolvable:
 * 1. indexed files of the project — `platformhome.properties`, `env.properties`, `advanced.properties`,
 *    `project.properties` of every configured extension and `local.properties` of the config extension;
 * 2. files referenced by the chain itself — the optional config directory and the runtime properties file;
 * 3. values injected by the running process — `platformhome` and the `env.properties.prefix` environment variables.
 */
@Service(Service.Level.PROJECT)
class CxPropertyCollector(private val project: Project) {

    /**
     * Cached snapshot of the chain. Acquires a read action on its own, so it is safe to call from anywhere.
     * Yields [CxPropertyModel.EMPTY] while the indexes are not ready yet.
     */
    fun collect(): CxPropertyModel {
        if (DumbService.isDumb(project)) return CxPropertyModel.EMPTY

        return application.runReadAction<CxPropertyModel> {
            CachedValuesManager.getManager(project).getCachedValue(project, CACHE_KEY, {
                val model = buildModel()
                // Nothing to track yet - never cache an empty chain, the files may still appear
                val dependencies = model.sources
                    .mapNotNull { it.file }
                    .takeIf { it.isNotEmpty() }
                    ?.plus(project.ySettings)
                    ?.toTypedArray()
                    ?: arrayOf<Any>(ModificationTracker.EVER_CHANGED)

                CachedValueProvider.Result.create(model, *dependencies)
            }, false)
        }
    }

    fun resetCache() = project.removeUserData(CACHE_KEY)

    private fun buildModel(): CxPropertyModel {
        val sources = mutableListOf<CxPropertySource>()
        val declarations = mutableListOf<CxPropertyDeclaration>()

        fun append(contributions: List<Pair<CxPropertySource, PropertiesFile>>) = contributions
            .forEach { (source, propertiesFile) ->
                sources.add(source)
                declarations.addAll(propertiesFile.declarationsOf(source))
            }

        append(collectIndexedFiles())
        append(collectReferencedFiles(CxPropertyModel.of(sources, declarations)))

        environmentDeclarations(CxPropertyModel.of(sources, declarations))
            .takeIf { it.isNotEmpty() }
            ?.let {
                sources.add(ENVIRONMENT_SOURCE)
                declarations.addAll(it)
            }

        return CxPropertyModel.of(sources, declarations)
    }

    private fun collectIndexedFiles(): List<Pair<CxPropertySource, PropertiesFile>> {
        val psiManager = PsiManager.getInstance(project)
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        val moduleMapping = project.ySettings.module2extensionMapping
        val extensionRanks = extensionRanks()

        return FileTypeIndex.getFiles(PropertiesFileType.INSTANCE, searchScope())
            .mapNotNull { virtualFile ->
                val scope = CxPropertyScope.of(virtualFile.name) ?: return@mapNotNull null
                val extension = projectFileIndex.getModuleForFile(virtualFile)
                    ?.yExtensionName(moduleMapping)

                // The platform reads `project.properties` of the configured extensions only
                if (scope == CxPropertyScope.PROJECT && extensionRanks.isNotEmpty() && extension !in extensionRanks) return@mapNotNull null

                val propertiesFile = psiManager.findFile(virtualFile)
                    ?.asSafely<PropertiesFile>()
                    ?: return@mapNotNull null

                val source = CxPropertySource(
                    name = virtualFile.name,
                    scope = scope,
                    rank = if (scope == CxPropertyScope.PROJECT) extensionRanks[extension] ?: extensionRanks.size else 0,
                    extension = extension,
                    path = virtualFile.path,
                    file = virtualFile,
                )

                source to propertiesFile
            }
    }

    private fun collectReferencedFiles(chain: CxPropertyModel): List<Pair<CxPropertySource, PropertiesFile>> {
        val referenced = mutableListOf<Pair<CxPropertySource, PropertiesFile>>()

        optionalConfigDirectory(chain)
            ?.listFiles { _, name -> OPTIONAL_CONFIG_FILE.matcher(name).matches() }
            ?.associateByTo(TreeMap()) { it.name }
            ?.values
            ?.forEachIndexed { rank, file ->
                toPropertiesFile(file)
                    ?.let { referenced.add(externalSource(file, CxPropertyScope.OPTIONAL_CONFIG, rank, it) to it) }
            }

        System.getenv(HybrisConstants.ENV_HYBRIS_RUNTIME_PROPERTIES)
            ?.takeIf { it.isNotBlank() }
            ?.let { File(it) }
            ?.let { file ->
                toPropertiesFile(file)
                    ?.let { referenced.add(externalSource(file, CxPropertyScope.RUNTIME, 0, it) to it) }
            }

        return referenced
    }

    private fun environmentDeclarations(chain: CxPropertyModel): List<CxPropertyDeclaration> {
        val declarations = mutableListOf<CxPropertyDeclaration>()

        platformHome()
            ?.let { declarations.add(CxPropertyDeclaration(HybrisConstants.PROPERTY_PLATFORMHOME, it, ENVIRONMENT_SOURCE)) }

        chain.resolve(HybrisConstants.PROPERTY_ENV_PROPERTY_PREFIX)
            ?.takeIf { it.isNotBlank() }
            ?.let { prefix ->
                System.getenv()
                    .filter { it.key.startsWith(prefix) }
                    .forEach { (name, value) ->
                        declarations.add(CxPropertyDeclaration(name.substring(prefix.length).asPropertyKey(), value, ENVIRONMENT_SOURCE))
                    }
            }

        return declarations
    }

    /**
     * Ranks the configured extensions the way the platform loads them — an extension after everything it requires,
     * the platform itself always first.
     */
    private fun extensionRanks(): Map<String, Int> {
        val descriptors = project.ySettings.extensionDescriptors
            .filter { it.type in LOADED_EXTENSION_TYPES }
            .sortedWith(compareBy({ if (it.type == ModuleDescriptorType.PLATFORM) 0 else 1 }, { it.name }))
            .takeIf { it.isNotEmpty() }
            ?: return emptyMap()

        val requiredByAll = descriptors
            .filter { it.getContext()?.requiredByAll == true }
            .map { it.name }
            .toSet()

        val requirements = descriptors.associateTo(LinkedHashMap()) { it.name to it.requirements(requiredByAll) }

        return CxExtensionLoadOrder.ranks(requirements)
    }

    private fun ExtensionDescriptor.requirements(requiredByAll: Set<String>): List<String> {
        if (type == ModuleDescriptorType.PLATFORM) return emptyList()

        val required = getContext()
            ?.requiredExtensions
            ?.map { it.name }
            ?: emptyList()

        return (required + requiredByAll)
            .distinct()
            .filterNot { it == name }
    }

    private fun optionalConfigDirectory(chain: CxPropertyModel) = (System.getenv(HybrisConstants.ENV_HYBRIS_OPT_CONFIG_DIR)
        ?: chain.resolve(HybrisConstants.PROPERTY_OPTIONAL_CONFIG_DIR))
        ?.takeIf { it.isNotBlank() }
        ?.let { File(it) }
        ?.takeIf { it.isDirectory }

    private fun platformHome() = project.yModule(EiConstants.Extension.PLATFORM)
        ?.let { ModuleRootManager.getInstance(it) }
        ?.contentRoots
        ?.firstOrNull { it.findChild(ProjectConstants.File.EXTENSIONS_XML) != null }
        ?.path

    private fun searchScope(): GlobalSearchScope {
        val scopes = mutableListOf(
            GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.everythingScope(project), PropertiesFileType.INSTANCE)
                .withFileNames(ProjectConstants.File.PROJECT_PROPERTIES, ProjectConstants.File.PLATFORM_HOME_PROPERTIES)
        )

        project.yModule(EiConstants.Extension.PLATFORM)
            ?.let { scopes.add(it.moduleContentScope.withFileNames(ProjectConstants.File.ENV_PROPERTIES, ProjectConstants.File.ADVANCED_PROPERTIES)) }
        project.yModule(EiConstants.Extension.CONFIG)
            ?.let { scopes.add(it.moduleContentScope.withFileNames(ProjectConstants.File.LOCAL_PROPERTIES)) }

        return GlobalSearchScope.union(scopes)
    }

    private fun externalSource(file: File, scope: CxPropertyScope, rank: Int, propertiesFile: PropertiesFile) = CxPropertySource(
        name = file.name,
        scope = scope,
        rank = rank,
        path = file.path,
        file = propertiesFile.virtualFile,
    )

    private fun toPropertiesFile(file: File) = LocalFileSystem.getInstance().findFileByIoFile(file)
        ?.takeIf { it.exists() }
        ?.let { PsiManager.getInstance(project).findFile(it) }
        ?.asSafely<PropertiesFile>()

    private fun PropertiesFile.declarationsOf(source: CxPropertySource) = properties
        .mapNotNull { property ->
            val key = property.key ?: return@mapNotNull null
            CxPropertyDeclaration(key, property.value ?: "", source)
        }

    private fun GlobalSearchScope.withFileNames(vararg names: String) = object : DelegatingGlobalSearchScope(this) {
        override fun contains(file: VirtualFile) = file.name in names && super.contains(file)
    }

    /** `SAP_CX_my__property_name` becomes `my_property.name`, the way the platform un-escapes environment variables. */
    private fun String.asPropertyKey() = replace("__", "##")
        .replace("_", ".")
        .replace("##", "_")

    companion object {
        private val CACHE_KEY = Key.create<CachedValue<CxPropertyModel>>("sap.commerce.toolset.properties.meta")
        private val OPTIONAL_CONFIG_FILE: Pattern = Pattern.compile("([1-9]\\d)-(\\w*)\\.properties")
        private val LOADED_EXTENSION_TYPES = setOf(
            ModuleDescriptorType.PLATFORM,
            ModuleDescriptorType.EXT,
            ModuleDescriptorType.OOTB,
            ModuleDescriptorType.CUSTOM,
        )
        private val ENVIRONMENT_SOURCE = CxPropertySource("environment", CxPropertyScope.ENVIRONMENT)

        fun getInstance(project: Project): CxPropertyCollector = project.service()
    }
}
