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
package sap.commerce.toolset.java.jarFinder

import com.intellij.ide.IdeCoreBundle
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.getOrCreateUserDataUnsafe
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.io.HttpRequests
import kotlinx.serialization.json.Json
import sap.commerce.toolset.java.configurator.ArtifactType
import sap.commerce.toolset.java.configurator.MavenCoords
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.util.*
import java.util.jar.Attributes
import java.util.jar.JarFile

/**
 * Custom implementation of the maven artifact searcher required to support caching and advanced groupId identification and better artifact search by hash.
 *
 * TODO: support "Remote Jar Repositories", instead of hardcoding `repo1.maven.org`
 */
@Service
class SonatypeCentralSourceSearcher {

    private val baseUrl = "https://repo1.maven.org/maven2"

    fun findSourceJar(indicator: ProgressIndicator, classesJar: VirtualFile, artifactType: ArtifactType): String? {
        try {
            indicator.text = IdeCoreBundle.message("progress.message.connecting.to", "https://central.sonatype.com")
            indicator.checkCanceled()

            val mavenCoords = classesJar.getOrCreateUserDataUnsafe(KEY_MAVEN_COORDS) {
                // most common approach -> maven packaging META-INF/maven
                val coords = readMavenCoordsFromArchive(classesJar)
                // example: accessors-smart-2.5.2.jar
                    ?: readBundleManifestMF(classesJar)
                    // example: activation-1.1.1.jar
                    ?: readExtensionManifestMF(classesJar)
                    // if nothing helps -> fallback to search by SHA1 of the respective jar file
                    ?: getExternalMavenCoords(indicator, "https://central.sonatype.com/solrsearch/select?rows=1&wt=json&q=1:${classesJar.toNioPath().toFile().sha1()}")
                        ?.let { MavenCoords.from(it) }
                // cache it only if artifact exists in remote
                coords?.takeIf { remoteExists(coords.toUrl(baseUrl)) }
            } ?: return null

            return classesJar.getOrCreateUserDataUnsafe(artifactType.key) {
                mavenCoords.toUrl(baseUrl, artifactType)
                    .takeIf { artifactUrl -> remoteExists(artifactUrl) }
            }
        } catch (e: IOException) {
            indicator.checkCanceled() // Cause of IOException may be canceling of operation.
            thisLogger().debug(e.message)
        }

        return null
    }

    private fun getExternalMavenCoords(indicator: ProgressIndicator, url: String) = try {
        HttpRequests.request(url)
            .accept("application/json")
            .connectTimeout(3000)
            .readTimeout(3000)
            .connect { processor ->
                JSON.decodeFromString<SolrResponse>(processor.readString(indicator)).response
                    .docs
                    .firstOrNull()
            }
    } catch (e: Exception) {
        thisLogger().debug(e.message)
        null
    }

    private fun remoteExists(url: String) = try {
        HttpRequests.head(url)
            .connectTimeout(3000)
            .readTimeout(3000)
            .connect { processor ->
                processor.connection
                    .asSafely<HttpURLConnection>()
                    ?.responseCode == HttpURLConnection.HTTP_OK
            }
    } catch (e: Exception) {
        thisLogger().debug(e.message)
        false
    }

    private fun readBundleManifestMF(classesJar: VirtualFile) = readManifestMF(classesJar) { attributes ->
        val bundleSymbolicName = attributes.getValue("Bundle-SymbolicName") ?: return@readManifestMF null
        val version = attributes.getValue("Bundle-Version") ?: return@readManifestMF null
        val jarName = classesJar.nameWithoutExtension
        val groupId = "$bundleSymbolicName-$version".removeSuffix(jarName)
        val artifactId = jarName.removeSuffix("-$version")

        MavenCoords(groupId, artifactId, version, "manifest-bundle")
    }

    private fun readExtensionManifestMF(classesJar: VirtualFile) = readManifestMF(classesJar) { attributes ->
        val groupId = attributes.getValue("Extension-Name") ?: return@readManifestMF null
        val version = attributes.getValue("Implementation-Version") ?: return@readManifestMF null
        val jarName = classesJar.nameWithoutExtension
        val artifactId = jarName.removeSuffix("-$version")

        MavenCoords(groupId, artifactId, version, "manifest-extension")
    }

    private fun readManifestMF(classesJar: VirtualFile, mapper: (Attributes) -> MavenCoords?): MavenCoords? {
        try {
            return JarFile(VfsUtilCore.virtualToIoFile(classesJar))
                .use { jarFile ->
                    jarFile.manifest?.mainAttributes
                        ?.let { attributes -> mapper(attributes) }
                }
        } catch (e: IOException) {
            thisLogger().debug(e.message)
        }

        return null
    }

    private fun readMavenCoordsFromArchive(classesJar: VirtualFile): MavenCoords? {
        try {
            JarFile(VfsUtilCore.virtualToIoFile(classesJar)).use { jarFile ->
                val entries = jarFile.entries()

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.getName()
                    if (REGEX_POM_PROPERTIES.matches(name)) {
                        return jarFile.getInputStream(entry).use { pomEntry ->
                            val props = Properties().apply { load(pomEntry) }

                            MavenCoords(
                                groupId = props.getProperty("groupId"),
                                artifactId = props.getProperty("artifactId"),
                                version = props.getProperty("version"),
                                source = "archive",
                            )
                        }
                    }
                }
            }
        } catch (e: IOException) {
            thisLogger().debug(e.message)
        }

        return null
    }

    private fun File.sha1(): String {
        val digest = MessageDigest.getInstance("SHA-1")
        inputStream().use { fis ->
            val buffer = ByteArray(1024)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val REGEX_POM_PROPERTIES = "META-INF/maven.+/pom\\.properties".toRegex()
        private val KEY_MAVEN_COORDS: Key<MavenCoords?> = Key.create<MavenCoords?>("sap.commerce.toolset.java.jarArtifactUrl")
        private val JSON = Json { ignoreUnknownKeys = true }

        fun getService(): SonatypeCentralSourceSearcher = application.service()
    }
}
