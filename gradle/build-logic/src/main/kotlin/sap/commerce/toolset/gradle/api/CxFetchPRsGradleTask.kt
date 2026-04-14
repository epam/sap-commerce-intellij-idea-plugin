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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import sap.commerce.toolset.gradle.api.dto.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

/**
 * Fetches PR metadata from GitHub repository using GraphQL API.
 * Caches results based on latest commit SHA to avoid redundant API calls.
 * Only refetches if repository HEAD has changed since last execution.
 * Filters PRs by specified labels and outputs JSON to resources directory.
 */
abstract class CxFetchPRsGradleTask : DefaultTask() {

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
    fun execute() = githubToken.orNull
        ?.let { gitHubToken -> executeGraphQL(gitHubToken) }
        ?: executeRest()

    private fun executeGraphQL(gitHubToken: String) {
        val client = HttpClient.newHttpClient()
        val gson = GsonBuilder().setPrettyPrinting().create()

        val repo = repository.get()
        val labels = targetLabels.get()
        val (owner, repoName) = repo.split("/")

        logger.lifecycle("Fetching import/refresh related PRs from the remote: $repo | ${branch.get()}")

        // Get latest commit SHA
        val commitRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$repo/commits/${branch.get()}"))
            .header("Accept", "application/vnd.github.v3+json")
            .header("Authorization", "Bearer $gitHubToken")
            .GET()
            .build()

        val commitResponse = client.send(commitRequest, HttpResponse.BodyHandlers.ofString())
        if (commitResponse.statusCode() != 200) {
            throw RuntimeException("Failed to fetch commit: ${commitResponse.statusCode()}")
        }

        val commitData = gson.fromJson(commitResponse.body(), CommitResponse::class.java)
        val latestCommit = commitData.sha

        // Check if already fetched for this commit
        val metadata = metadataFile.get().asFile
        val output = outputFile.get().asFile

        val cachedCommit = if (metadata.exists()) {
            gson.fromJson(metadata.readText(), Metadata::class.java).lastCommit
        } else null

        if (cachedCommit == latestCommit && output.exists()) {
            logger.lifecycle("Already up-to-date (commit: ${latestCommit.take(7)})")
            logger.lifecycle("Skipping fetch")
            return
        }

        logger.lifecycle("New commit detected: ${latestCommit.take(7)}")

        // Fetch all PRs in parallel batches
        val allNodes = mutableListOf<PRNode>()
        var endCursor: String? = null
        var hasNextPage = true

        while (hasNextPage) {
            val cursors = mutableListOf<String?>()
            cursors.add(endCursor)

            // Prefetch next cursors for parallel requests
            val futures = cursors.map { cursor ->
                CompletableFuture.supplyAsync {
                    fetchPage(client, gson, owner, repoName, labels, gitHubToken, cursor)
                }
            }

            val results = futures.map { it.get() }

            results.forEach { result ->
                allNodes.addAll(result.nodes)
            }

            val lastResult = results.last()
            hasNextPage = lastResult.pageInfo.hasNextPage
            endCursor = lastResult.pageInfo.endCursor

            logger.lifecycle("Fetched ${allNodes.size} PRs so far...")
        }

        val filteredPRs = allNodes
            .filter { pr -> pr.labels.nodes.any { it.name in labels } }
            .map { pr ->
                PullRequest(
                    title = pr.title,
                    number = pr.number,
                    author = pr.author?.login,
                    milestone = pr.milestone?.title,
                    labels = pr.labels.nodes.map { it.name }
                )
            }

        output.parentFile.mkdirs()
        output.writeText(gson.toJson(PRData(filteredPRs)))

        metadata.parentFile.mkdirs()
        metadata.writeText(gson.toJson(Metadata(latestCommit, filteredPRs.size)))

        logger.lifecycle("Fetched ${filteredPRs.size} PRs")
        logger.lifecycle("Output: ${output.absolutePath}")
    }

    private fun executeRest() {
        val client = HttpClient.newHttpClient()
        val gson = GsonBuilder().setPrettyPrinting().create()

        val repo = repository.get()
        val labels = targetLabels.get()

        logger.lifecycle(
            """
                GITHUB_TOKEN not provided, using REST API fallback (rate-limited).
        
                Fetching import/refresh related PRs from: $repo | ${branch.get()}
            """.trimIndent()
        )

        // Get latest commit SHA without auth
        val commitRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$repo/commits/${branch.get()}"))
            .header("Accept", "application/vnd.github.v3+json")
            .GET()
            .build()

        val commitResponse = client.send(commitRequest, HttpResponse.BodyHandlers.ofString())
        if (commitResponse.statusCode() != 200) {
            throw RuntimeException("Failed to fetch commit: ${commitResponse.statusCode()}")
        }

        val commitData = gson.fromJson(commitResponse.body(), CommitResponse::class.java)
        val latestCommit = commitData.sha

        // Check if already fetched for this commit
        val metadata = metadataFile.get().asFile
        val output = outputFile.get().asFile

        val cachedCommit = if (metadata.exists()) {
            gson.fromJson(metadata.readText(), Metadata::class.java).lastCommit
        } else null

        if (cachedCommit == latestCommit && output.exists()) {
            logger.lifecycle("Already up-to-date (commit: ${latestCommit.take(7)})")
            logger.lifecycle("Skipping fetch")
            return
        }

        logger.lifecycle("New commit detected: ${latestCommit.take(7)}")

        // Fetch PRs for each label separately (REST API doesn't support multiple labels in one query)
        val allPRs = mutableMapOf<Int, PullRequest>() // Deduplicate by PR number

        labels.forEach { label ->
            var page = 1
            var hasMore = true

            while (hasMore && page <= 5) { // Limit pages per label
                val labelsParam = label.replace(" ", "+")
                val prRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/$repo/issues?state=closed&labels=$labelsParam&per_page=100&page=$page"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build()

                val prResponse = client.send(prRequest, HttpResponse.BodyHandlers.ofString())

                if (prResponse.statusCode() == 403) {
                    logger.warn("Rate limit exceeded. Set GITHUB_TOKEN to avoid this.")
                    hasMore = false
                    break
                }

                if (prResponse.statusCode() != 200) {
                    throw RuntimeException("Failed to fetch PRs: ${prResponse.statusCode()}")
                }

                val pagePRs = gson.fromJson(prResponse.body(), Array<RestPR>::class.java)

                if (pagePRs.isEmpty()) {
                    hasMore = false
                    break
                }

                pagePRs
                    .filter { it.pull_request != null } // Issues endpoint returns both issues and PRs
                    .forEach { pr ->
                        allPRs.putIfAbsent(
                            pr.number,
                            PullRequest(
                                title = pr.title,
                                number = pr.number,
                                author = pr.user?.login,
                                milestone = pr.milestone?.title,
                                labels = pr.labels.map { it.name }
                            )
                        )
                    }

                logger.lifecycle("Label '$label', page $page - ${allPRs.size} total PRs so far...")
                page++
            }
        }

        val prs = allPRs.values.toList()

        output.parentFile.mkdirs()
        output.writeText(gson.toJson(PRData(prs)))

        metadata.parentFile.mkdirs()
        metadata.writeText(gson.toJson(Metadata(latestCommit, prs.size)))

        logger.lifecycle("Fetched ${prs.size} PRs (fallback mode)")
        logger.lifecycle("Output: ${output.absolutePath}")
    }

    private fun fetchPage(
        client: HttpClient,
        gson: Gson,
        owner: String,
        repoName: String,
        labels: List<String>,
        token: String,
        cursor: String?
    ): PullRequestsPage {
        val afterClause = cursor?.let { """, after: "$it"""" } ?: ""
        val labelsClause = labels.joinToString(",") { "\"$it\"" }
        val statusesClause = "CLOSED, MERGED"
        val query = """
        query {
          repository(owner: "$owner", name: "$repoName") {
            pullRequests(first: 100 $afterClause, labels: [$labelsClause], states: [$statusesClause], orderBy: {field: UPDATED_AT, direction: DESC}) {
              pageInfo {
                hasNextPage
                endCursor
              }
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

        val result = gson.fromJson(graphqlResponse.body(), GraphQLResponse::class.java)
        return PullRequestsPage(
            result.data.repository.pullRequests.pageInfo,
            result.data.repository.pullRequests.nodes
        )
    }
}
