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
import com.intellij.jarFinder.SourceSearcher
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import sap.commerce.toolset.java.configurator.ArtifactType
import java.io.IOException

/**
 * Custom implementation of the maven artifact searcher required to support caching and
 */
class SonatypeCentralSourceSearcher(private val artifactType: ArtifactType) : SourceSearcher() {

    override fun findSourceJar(
        indicator: ProgressIndicator,
        artifactId: String,
        version: String,
        classesJar: VirtualFile
    ): String? = try {
        indicator.text = IdeCoreBundle.message("progress.message.connecting.to", "https://central.sonatype.com")
        indicator.checkCanceled()

        classesJar.getUserData(KEY_ARTIFACT_URL_PREFIX)
            ?.let { "$it-${artifactType.mavenPostfix}.jar" }
            ?: findElements("./response/docs/docs/g", readElementCancelable(indicator, buildUrl(classesJar, artifactId, version)))
                .map { it.value }
                .map { it.replace('.', '/') }
                .map { groupId -> "https://repo1.maven.org/maven2/$groupId/$artifactId/$version/$artifactId-$version-${artifactType.mavenPostfix}.jar" }
                .firstOrNull()
                .also { groupId ->
                    classesJar.putUserData(
                        KEY_ARTIFACT_URL_PREFIX,
                        "https://repo1.maven.org/maven2/$groupId/$artifactId/$version/$artifactId-$version"
                    )
                }
    } catch (e: IOException) {
        indicator.checkCanceled() // Cause of IOException may be canceling of operation.

        thisLogger().warn(e)

        null
    }

    private fun buildUrl(classesJar: VirtualFile, artifactId: String, version: String): String = buildString {
        append("https://central.sonatype.com/solrsearch/select?rows=3&wt=xml&q=")

        findMavenGroupId(classesJar, artifactId)
            ?.let { groupId -> append("g:$groupId%20AND%20") }

        append("a:$artifactId%20AND%20v:$version%20AND%20l:${artifactType.mavenPostfix}")
    }

    companion object {
        private val KEY_ARTIFACT_URL_PREFIX = Key.create<String>("sap.commerce.toolset.java.jarArtifactUrl")
    }
}
