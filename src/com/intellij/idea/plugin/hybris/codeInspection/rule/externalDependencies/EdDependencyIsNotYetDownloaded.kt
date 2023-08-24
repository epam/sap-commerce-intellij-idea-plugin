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
package com.intellij.idea.plugin.hybris.codeInspection.rule.externalDependencies

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel
import org.jetbrains.idea.maven.indices.archetype.MavenCatalog
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class EdDependencyIsNotYetDownloaded : AbstractEdInspection() {

    override fun inspect(
        project: Project,
        dom: MavenDomProjectModel,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        val libDirectory = holder.fileElement
            .file
            .parent
            ?.findSubdirectory("lib") ?: return

        dom.dependencies.dependencies
            .forEach {
                val artifactId = it.artifactId.stringValue ?: return@forEach
                val version = it.version.stringValue ?: return@forEach
                val library = "$artifactId-$version.jar"

                if (libDirectory.findFile(library) == null) {
                    holder.createProblem(
                        it,
                        severity,
                        HybrisI18NBundleUtils.message("hybris.inspections.ed.EdDependencyIsNotYetDownloaded.problem", library),
                        TextRange.from(0, it.xmlTag?.textLength!! - 1)
                    )
                }
            }
//
//        val groupId = "com.example"
//        val artifactId = "your-artifact-id"
//        val version = "1.0.0"
//        val targetModulePath = "/path/to/your/module"
//
//        downloadMavenDependencyWithSources(groupId, artifactId, version, targetModulePath)

    }

    fun downloadFile(url: String, targetFile: File) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()

        val inputStream = connection.inputStream
        val outputStream = FileOutputStream(targetFile)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    fun downloadMavenDependencyWithSources(
        groupId: String,
        artifactId: String,
        version: String,
        targetModulePath: String
    ) {
        val repositoryUrl = MavenCatalog.System.MavenCentral.url
        val jarUrl = "$repositoryUrl/${groupId.replace('.', '/')}/$artifactId/$version/$artifactId-$version.jar"
        val sourcesUrl = "$repositoryUrl/${groupId.replace('.', '/')}/$artifactId/$version/$artifactId-$version-sources.jar"

        val libFolder = File("$targetModulePath/lib")
        libFolder.mkdirs()

        runBlocking(Dispatchers.IO) {
            val jarFile = File(libFolder, "$artifactId-$version.jar")
            val sourcesFile = File(libFolder, "$artifactId-$version-sources.jar")

            downloadFile(jarUrl, jarFile)
            downloadFile(sourcesUrl, sourcesFile)

            println("Dependency downloaded and copied to lib folder.")
        }
    }
}
