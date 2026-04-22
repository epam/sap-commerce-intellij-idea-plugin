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
import com.intellij.openapi.project.Project
import com.intellij.util.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.util.fileExists
import sap.commerce.toolset.welcomescreen.presentation.HostingEnvironment
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProjectSettings
import java.nio.file.Files
import kotlin.coroutines.cancellation.CancellationException

/**
 * Stateless reader for `.idea/hybrisProjectSettings.xml`.
 *
 * Reads the file line-by-line looking for known option names. This assumes
 * the file is plugin-generated with predictable one-option-per-line
 * formatting and no inline comments — valid for files produced by this
 * plugin.
 *
 * Individual fields are `null` when:
 *   - the option line is absent
 *   - the `value="..."` attribute is missing or its contents are blank
 *
 * Callers treat `null` fields as "unknown" — UI shows a placeholder.
 */
@Service
internal class SapCommerceProjectSettingsReader : LazyRecentProjectDetailsReader<RecentSapCommerceProjectSettings> {

    override suspend fun read(recentProject: RecentSapCommerceProject): RecentSapCommerceProjectSettings {
        val settingsFile = recentProject.path
            .resolve(Project.DIRECTORY_STORE_FOLDER)
            .resolve(HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS)
            .takeIf { it.fileExists }
            ?: return RecentSapCommerceProjectSettings.NotLoaded

        var hybrisVersion: String? = null
        var hostingEnvironment: String? = null

        try {
            withContext(Dispatchers.IO) {
                Files.newBufferedReader(settingsFile).use { reader ->
                    for (line in reader.lineSequence()) {
                        when {
                            HYBRIS_VERSION_MARKER in line -> hybrisVersion = extractValue(line)
                            HOSTING_ENVIRONMENT_MARKER in line -> hostingEnvironment = extractValue(line)
                        }
                        if (hybrisVersion != null && hostingEnvironment != null) break
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            thisLogger().debug(e)
        }

        return RecentSapCommerceProjectSettings.Loaded(
            hybrisVersion = hybrisVersion,
            hostingEnvironment = HostingEnvironment.of(hostingEnvironment)
        )
    }

    /** Extracts the contents of `value="..."` from a line, or `null` if absent/blank. */
    private fun extractValue(line: String): String? {
        val start = line.indexOf(VALUE_ATTR).takeIf { it >= 0 } ?: return null
        val openQuote = start + VALUE_ATTR.length
        val closeQuote = line.indexOf('"', openQuote).takeIf { it > openQuote } ?: return null
        return line.substring(openQuote, closeQuote)
            .trim()
            .takeIf { it.isNotEmpty() }
    }

    companion object {
        private const val HYBRIS_VERSION_MARKER = "name=\"hybrisVersion\""
        private const val HOSTING_ENVIRONMENT_MARKER = "name=\"hostingEnvironment\""
        private const val VALUE_ATTR = "value=\""

        fun getInstance(): SapCommerceProjectSettingsReader = application.service()
    }
}