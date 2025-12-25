package sap.commerce.toolset.project.localextensions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.PropertiesUtil
import com.intellij.util.application
import com.intellij.util.asSafely
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.localextensions.jaxb.Hybrisconfig
import sap.commerce.toolset.localextensions.jaxb.ObjectFactory
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.max

// TODO: move to core
@Service
class ProjectLocalExtensionsScanner {

    fun processHybrisConfig(
        importContext: ProjectImportContext.Mutable,
        configModuleDescriptor: ConfigModuleDescriptor
    ): Set<String> = ApplicationManager.getApplication().runReadAction(Computable {
        val hybrisConfig = unmarshalLocalExtensions(configModuleDescriptor)
            ?: return@Computable setOf()

        val explicitlyDefinedModules = TreeSet(String.CASE_INSENSITIVE_ORDER)

        processHybrisConfigExtensions(hybrisConfig, explicitlyDefinedModules)
        processHybrisConfigAutoloadPaths(importContext, hybrisConfig, explicitlyDefinedModules)
        explicitlyDefinedModules
    })

    private fun processHybrisConfigAutoloadPaths(
        importContext: ProjectImportContext.Mutable,
        hybrisConfig: Hybrisconfig,
        explicitlyDefinedModules: TreeSet<String>
    ) {
        if (importContext.platformDirectory == null) return

        val autoloadPaths = HashMap<String, Int>()

        hybrisConfig.getExtensions().getPath()
            .filter { it.isAutoload }
            .filter { it.dir != null }
            .forEach {
                val depth = it.depth
                val dir = it.dir!!

                if (depth == null) autoloadPaths[dir] = HybrisConstants.DEFAULT_EXTENSIONS_PATH_DEPTH
                else if (depth > 1) {
                    if (!autoloadPaths.containsKey(dir)) autoloadPaths[dir] = depth
                    else autoloadPaths.computeIfPresent(dir) { _: String, oldValue: Int ->
                        max(oldValue, depth)
                    }
                }
            }

        if (autoloadPaths.isEmpty()) return

        val platform = Paths.get(importContext.platformDirectory!!.getPath(), HybrisConstants.PLATFORM_MODULE_PREFIX).toString()
        val path = Paths.get(platform, "env.properties")

        try {
            Files.newBufferedReader(path, StandardCharsets.ISO_8859_1).use { fis ->
                val properties = PropertiesUtil.loadProperties(fis)

                properties.entries.forEach {
                    val value = it.value.replace("\${platformhome}", platform)
                    it.setValue(Paths.get(value).normalize().toString())
                }
                properties["platformhome"] = platform

                val normalizedPaths = autoloadPaths.entries
                    .associate { entry ->
                        val key = properties.entries
                            .filter { property -> entry.key.contains("\${" + property.key + '}') }
                            .firstNotNullOfOrNull { property -> entry.key.replace("\${" + property.key + '}', property.value) }
                            ?: entry.key
                        val normalizedKey = Paths.get(key).normalize().toString()

                        normalizedKey to entry.value
                    }

                importContext.foundModules.forEach {
                    for (entry in normalizedPaths.entries) {
                        val moduleDir = it.moduleRootDirectory.path
                        if (moduleDir.startsWith(entry.key)
                            && Paths.get(moduleDir.substring(entry.key.length)).nameCount <= entry.value
                        ) {
                            explicitlyDefinedModules.add(it.name)
                            break
                        }
                    }
                }
            }
        } catch (_: IOException) {
            // NOP
        }
    }

    private fun processHybrisConfigExtensions(
        hybrisConfig: Hybrisconfig,
        explicitlyDefinedModules: TreeSet<String>
    ) {
        for (extensionType in hybrisConfig.getExtensions().getExtension()) {
            val name = extensionType.getName()
            if (name != null) {
                explicitlyDefinedModules.add(name)
                continue
            }
            val dir = extensionType.getDir()

            if (dir == null) continue

            val indexSlash = dir.lastIndexOf('/')
            val indexBack = dir.lastIndexOf('\\')
            val index = max(indexSlash, indexBack)
            if (index == -1) {
                explicitlyDefinedModules.add(dir)
            } else {
                explicitlyDefinedModules.add(dir.substring(index + 1))
            }
        }
    }

    private fun unmarshalLocalExtensions(configModuleDescriptor: ConfigModuleDescriptor): Hybrisconfig? {
        val file = File(configModuleDescriptor.moduleRootDirectory, HybrisConstants.LOCAL_EXTENSIONS_XML)
        if (!file.exists()) return null

        try {
            return JAXBContext.newInstance(
                ObjectFactory::class.java.getPackageName(),
                ObjectFactory::class.java.getClassLoader()
            )
                .createUnmarshaller()
                .unmarshal(file)
                .asSafely<Hybrisconfig>()
        } catch (e: JAXBException) {
            thisLogger().error("Can not unmarshal ${file.absolutePath}", e)
        }

        return null
    }

    companion object {
        fun getInstance(): ProjectLocalExtensionsScanner = application.service()
    }
}
