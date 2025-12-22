package sap.commerce.toolset.project.localextensions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.PropertiesUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.application
import com.intellij.util.asSafely
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import org.apache.commons.io.FilenameUtils
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.localextensions.jaxb.Hybrisconfig
import sap.commerce.toolset.localextensions.jaxb.ObjectFactory
import sap.commerce.toolset.project.HybrisProjectService
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.factories.ModuleDescriptorFactory
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.math.max

@Service
class ProjectImportLocalExtensionsScanner {

    fun findConfigDir(projectDescriptor: HybrisProjectDescriptor): ConfigModuleDescriptor? {
        val foundConfigModules = mutableListOf<ConfigModuleDescriptor>()
        var platformHybrisModuleDescriptor: PlatformModuleDescriptor? = null
        projectDescriptor.foundModules.forEach { moduleDescriptor ->
            when (moduleDescriptor) {
                is ConfigModuleDescriptor -> foundConfigModules.add(moduleDescriptor)
                is PlatformModuleDescriptor -> platformHybrisModuleDescriptor = moduleDescriptor
            }
        }
        if (platformHybrisModuleDescriptor == null) {
            if (foundConfigModules.size == 1) return foundConfigModules.get(0)

            return null
        }
        val configDir: File?
        val externalConfigDirectory = projectDescriptor.externalConfigDirectory
        if (externalConfigDirectory != null) {
            configDir = externalConfigDirectory
            if (!configDir.isDirectory()) return null
        } else {
            configDir = getExpectedConfigDir(platformHybrisModuleDescriptor)
            if (configDir == null || !configDir.isDirectory()) {
                return if (foundConfigModules.size == 1) foundConfigModules[0]
                else null
            }
        }
        val configHybrisModuleDescriptor = foundConfigModules
            .firstOrNull { FileUtil.filesEqual(it.moduleRootDirectory, configDir) }
        if (configHybrisModuleDescriptor != null) return configHybrisModuleDescriptor

        if (!HybrisProjectService.Companion.getInstance().isConfigModule(configDir)) return null

        return try {
            val configHybrisModuleDescriptor = ModuleDescriptorFactory.createConfigDescriptor(
                configDir, platformHybrisModuleDescriptor.rootProjectDescriptor, configDir.getName()
            )
            thisLogger().info("Creating Overridden Config module in local.properties for ${configDir.absolutePath}")

            projectDescriptor.foundModules.add(configHybrisModuleDescriptor)
            projectDescriptor.foundModules.sort()

            configHybrisModuleDescriptor
        } catch (_: HybrisConfigurationException) {
            null
        }
    }

    fun processHybrisConfig(projectDescriptor: HybrisProjectDescriptor, yConfigModuleDescriptor: ModuleDescriptor): Set<String> {
        return ApplicationManager.getApplication().runReadAction(Computable {
            val hybrisconfig: Hybrisconfig? = unmarshalLocalExtensions(yConfigModuleDescriptor)
            if (hybrisconfig == null) return@Computable setOf()

            val explicitlyDefinedModules = TreeSet(String.CASE_INSENSITIVE_ORDER)

            processHybrisConfigExtensions(hybrisconfig, explicitlyDefinedModules)
            processHybrisConfigAutoloadPaths(hybrisconfig, explicitlyDefinedModules, projectDescriptor)
            explicitlyDefinedModules
        })
    }

    private fun getExpectedConfigDir(platformModuleDescriptor: PlatformModuleDescriptor): File? {
        val expectedConfigDir = Path(platformModuleDescriptor.moduleRootDirectory.absolutePath, HybrisConstants.CONFIG_RELATIVE_PATH)
        if (!expectedConfigDir.isDirectory()) return null

        val propertiesFile = expectedConfigDir.resolve(ProjectConstants.File.LOCAL_PROPERTIES)
        if (!propertiesFile.exists()) return expectedConfigDir.toFile()

        val properties = Properties()
        try {
            FileReader(propertiesFile.toFile()).use { fr ->
                properties.load(fr)
            }
        } catch (_: IOException) {
            return expectedConfigDir.toFile()
        }

        var hybrisConfig = properties[HybrisConstants.ENV_HYBRIS_CONFIG_DIR] as String?
        if (hybrisConfig == null) return expectedConfigDir.toFile()

        hybrisConfig = hybrisConfig.replace(
            HybrisConstants.PLATFORM_HOME_PLACEHOLDER,
            platformModuleDescriptor.moduleRootDirectory.path
        )
        hybrisConfig = FilenameUtils.separatorsToSystem(hybrisConfig)

        val hybrisConfigDir = Path(hybrisConfig)
        if (hybrisConfigDir.isDirectory()) return hybrisConfigDir.toFile()

        return expectedConfigDir.toFile()
    }

    private fun processHybrisConfigAutoloadPaths(hybrisconfig: Hybrisconfig, explicitlyDefinedModules: TreeSet<String>, projectDescriptor: HybrisProjectDescriptor) {
        if (projectDescriptor.hybrisDistributionDirectory == null) return

        val autoloadPaths = HashMap<String, Int>()

        hybrisconfig.getExtensions().getPath()
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

        val platform = Paths.get(projectDescriptor.hybrisDistributionDirectory!!.getPath(), HybrisConstants.PLATFORM_MODULE_PREFIX).toString()
        val path = Paths.get(platform, "env.properties")

        try {
            Files.newBufferedReader(path, StandardCharsets.ISO_8859_1).use { fis ->
                val properties = PropertiesUtil.loadProperties(fis)

                properties.entries.forEach {
                    val value = it.value.replace("\${platformhome}", platform)
                    it.setValue(Paths.get(value).normalize().toString())
                }
                properties.put("platformhome", platform)

                val normalizedPaths = autoloadPaths.entries
                    .associate { entry ->
                        val key = properties.entries
                            .filter { property -> entry.key.contains("\${" + property.key + '}') }
                            .firstNotNullOfOrNull { property -> entry.key.replace("\${" + property.key + '}', property.value) }
                            ?: entry.key
                        val normalizedKey = Paths.get(key).normalize().toString()

                        normalizedKey to entry.value
                    }

                projectDescriptor.foundModules.forEach {
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

    private fun processHybrisConfigExtensions(hybrisconfig: Hybrisconfig, explicitlyDefinedModules: TreeSet<String?>) {
        for (extensionType in hybrisconfig.getExtensions().getExtension()) {
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

    private fun unmarshalLocalExtensions(yConfigModuleDescriptor: ModuleDescriptor): Hybrisconfig? {
        val file = File(yConfigModuleDescriptor.moduleRootDirectory, HybrisConstants.LOCAL_EXTENSIONS_XML)
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
        fun getInstance(): ProjectImportLocalExtensionsScanner = application.service()
    }

}