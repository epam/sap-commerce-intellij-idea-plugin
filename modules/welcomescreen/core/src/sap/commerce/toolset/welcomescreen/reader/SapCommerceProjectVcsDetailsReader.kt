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

package sap.commerce.toolset.welcomescreen.reader

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.application
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.util.fileExists
import sap.commerce.toolset.welcomescreen.WelcomeScreenConstants
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProjectVcsDetails
import java.nio.file.Files

/**
 * Reads the current branch name (or short SHA, for detached HEAD) from a
 * project's `.git/HEAD` file.
 *
 * For a normal checkout the file contains `ref: refs/heads/<branch>` —
 * we strip the prefix and return the branch name.
 *
 * For a detached HEAD it contains a 40-character SHA — we return the first
 * 7 characters (matching git's default short-sha length).
 *
 * Returns `null` if the project isn't a git repository, the file is missing,
 * or the contents don't match either expected shape.
 */
@Service
internal class SapCommerceProjectVcsDetailsReader : LazyRecentProjectDetailsReader<RecentSapCommerceProjectVcsDetails> {

    override suspend fun read(recentProject: RecentSapCommerceProject): RecentSapCommerceProjectVcsDetails {
        return try {
            val headFile = recentProject.path
                .resolve(WelcomeScreenConstants.Vcs.GIT)
                .resolve(WelcomeScreenConstants.Vcs.COMMIT_HEAD)
                .takeIf { it.fileExists }
                ?: return RecentSapCommerceProjectVcsDetails.NotAGitRepo

            val contents = withContext(Dispatchers.IO) { Files.readString(headFile).trim() }
            when {
                contents.startsWith(REF_PREFIX) -> contents
                    .removePrefix(REF_PREFIX)
                    .takeIf { it.isNotBlank() }
                    ?.let { RecentSapCommerceProjectVcsDetails.Named(it) }
                    ?: RecentSapCommerceProjectVcsDetails.NotAGitRepo

                contents.matches(SHA_REGEX) -> contents
                    .substring(0, SHORT_SHA_LENGTH)
                    .let { RecentSapCommerceProjectVcsDetails.Named(it) }

                else -> RecentSapCommerceProjectVcsDetails.NotAGitRepo
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            thisLogger().debug(e)
            RecentSapCommerceProjectVcsDetails.NotAGitRepo
        }
    }

    companion object {
        private const val REF_PREFIX = "ref: refs/heads/"
        private const val SHORT_SHA_LENGTH = 7
        private val SHA_REGEX = Regex("^[0-9a-f]{40}$")

        fun getInstance(): SapCommerceProjectVcsDetailsReader = application.service()
    }
}