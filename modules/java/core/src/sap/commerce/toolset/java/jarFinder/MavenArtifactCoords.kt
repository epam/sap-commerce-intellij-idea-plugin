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

data class MavenArtifactCoords(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val source: String,
) {

    fun toUrl(baseUrl: String, libraryRootType: LibraryRootType) = groupId
        .let {
            val groupIdPath = groupId.replace('.', '/')
            "$baseUrl/$groupIdPath/$artifactId/$version/$artifactId-$version-${libraryRootType.mavenPostfix}.jar"
        }

    fun toUrl(baseUrl: String) = groupId
        .let {
            val groupIdPath = groupId.replace('.', '/')
            "$baseUrl/$groupIdPath/$artifactId/$version/$artifactId-$version.jar"
        }

    companion object {
        fun from(solrMavenArtifactCoords: SolrMavenArtifactCoords) = MavenArtifactCoords(
            solrMavenArtifactCoords.g,
            solrMavenArtifactCoords.a,
            solrMavenArtifactCoords.v,
            "solr"
        )
    }
}