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

package sap.commerce.toolset.gradle.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Fetches PR metadata from GitHub repository using GraphQL API.
 * Caches results based on latest commit SHA to avoid redundant API calls.
 * Only refetches if repository HEAD has changed since last execution.
 * Filters PRs by specified labels and outputs JSON to resources directory.
 */
abstract class FetchPRsGradleTask : DefaultTask() {

    @get:Input
    abstract val repository: Property<String>

    @get:Input
    abstract val targetLabels: ListProperty<String>

    @get:Input
    abstract val branch: Property<String>

    @get:Input
    @get:Optional
    abstract val githubToken: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:OutputFile
    abstract val metadataFile: RegularFileProperty

    init {
        branch.convention("master")
        githubToken.convention(System.getenv("GITHUB_TOKEN"))
    }

    @TaskAction
    fun execute() {
        val client = HttpClient.newHttpClient()
        val gson = GsonBuilder().setPrettyPrinting().create()

        val repo = repository.get()
        val labels = targetLabels.get()
        val (owner, repoName) = repo.split("/")
        val token = githubToken.orNull

        if (token == null) {
            throw RuntimeException(
                """
                GITHUB_TOKEN required. Set via environment or task configuration. Create new simple GitHub Token with the permissions to public repositories.

                To enforce project re-import / refresh it is expected to fetch PRs with labels "Project Refresh" or "Project Reimport".
                Analyze via HybrisProjectStructureStartupActivity and decide which action to be displayed to an end-user.
 
                This setting is mandatory for anyone build the plugin via `buildPlugin` task.
                """.trimIndent()
            )
        }

        // Get latest commit SHA
        val commitRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$repo/commits/${branch.get()}"))
            .header("Accept", "application/vnd.github.v3+json")
            .header("Authorization", "Bearer $token")
            .GET()
            .build()

        val commitResponse = client.send(commitRequest, HttpResponse.BodyHandlers.ofString())
        if (commitResponse.statusCode() != 200) {
            throw RuntimeException("Failed to fetch commit: ${commitResponse.statusCode()}")
        }

        val commitData = gson.fromJson(commitResponse.body(), Map::class.java)
        val latestCommit = commitData["sha"] as String

        // Check if already fetched for this commit
        val metadata = metadataFile.get().asFile
        val output = outputFile.get().asFile

        val cachedCommit = if (metadata.exists()) {
            val meta = gson.fromJson(metadata.readText(), Map::class.java)
            meta["lastCommit"] as? String
        } else null

        if (cachedCommit == latestCommit && output.exists()) {
            logger.lifecycle("Already up-to-date (commit: ${latestCommit.take(7)})")
            logger.lifecycle("Skipping fetch")
            return
        }

        logger.lifecycle("New commit detected: ${latestCommit.take(7)}")

        // GraphQL query
        val query = """
            query {
              repository(owner: "$owner", name: "$repoName") {
                pullRequests(first: 100, states: [OPEN, CLOSED, MERGED], orderBy: {field: UPDATED_AT, direction: DESC}) {
                  nodes {
                    title
                    number
                    author {
                      login
                    }
                    milestone {
                      title
                    }
                    labels(first: 20) {
                      nodes {
                        name
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val graphqlRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/graphql"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(mapOf("query" to query))))
            .build()

        val graphqlResponse = client.send(graphqlRequest, HttpResponse.BodyHandlers.ofString())

        if (graphqlResponse.statusCode() != 200) {
            throw RuntimeException("GraphQL API failed: ${graphqlResponse.statusCode()} - ${graphqlResponse.body()}")
        }

        val result = gson.fromJson(graphqlResponse.body(), Map::class.java)
        val data = result["data"] as? Map<String, Any>
        val repoData = data?.get("repository") as? Map<String, Any>
        val pullRequests = repoData?.get("pullRequests") as? Map<String, Any>
        val nodes = pullRequests?.get("nodes") as? List<Map<String, Any>> ?: emptyList()

        val filteredPRs = nodes.mapNotNull { pr ->
            val prLabels = (pr["labels"] as? Map<String, Any>)?.get("nodes") as? List<Map<String, Any>>
            val labelNames = prLabels?.map { it["name"] as String } ?: emptyList()

            if (labelNames.any { it in labels }) {
                val author = pr["author"] as? Map<String, Any>
                val milestone = pr["milestone"] as? Map<String, Any>

                mapOf(
                    "title" to pr["title"],
                    "number" to (pr["number"] as Double).toInt(),
                    "author" to author?.get("login"),
                    "milestone" to milestone?.get("title"),
                    "labels" to labelNames
                )
            } else null
        }

        output.parentFile.mkdirs()
        output.writeText(gson.toJson(mapOf("pullRequests" to filteredPRs)))

        metadata.parentFile.mkdirs()
        metadata.writeText(
            gson.toJson(
                mapOf(
                    "lastCommit" to latestCommit,
                    "totalPRs" to filteredPRs.size
                )
            )
        )

        logger.lifecycle("Fetched ${filteredPRs.size} PRs")
        logger.lifecycle("Output: ${output.absolutePath}")
    }
}