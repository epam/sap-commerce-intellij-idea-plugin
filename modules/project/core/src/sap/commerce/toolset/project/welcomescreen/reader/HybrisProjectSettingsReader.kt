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

package sap.commerce.toolset.project.welcomescreen.reader

import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.util.fileExists
import java.nio.file.Files
import java.nio.file.Path

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
object HybrisProjectSettingsReader {

    data class Settings(
        val hybrisVersion: String? = null
    )

    fun read(projectLocation: String): Settings {
        val settingsFile = Path.of(projectLocation)
            .resolve(ProjectConstants.Directory.IDEA)
            .resolve(HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS)

        if (!settingsFile.fileExists) return Settings()

        var hybrisVersion: String? = null

        runCatching {
            Files.newBufferedReader(settingsFile).use { reader ->
                for (line in reader.lineSequence()) {
                    if (HYBRIS_VERSION_MARKER in line) {
                        hybrisVersion = extractValue(line)
                        break  // only field we care about — stop reading
                    }
                }
            }
        }

        return Settings(hybrisVersion = hybrisVersion)
    }

    /** Extracts the contents of `value="..."` from a line, or `null` if absent/blank. */
    private fun extractValue(line: String): String? {
        val start = line.indexOf(VALUE_ATTR).takeIf { it >= 0 } ?: return null
        val openQuote = start + VALUE_ATTR.length
        val closeQuote = line.indexOf('"', openQuote).takeIf { it > openQuote } ?: return null
        return line.substring(openQuote, closeQuote).trim().takeIf { it.isNotEmpty() }
    }

    private const val HYBRIS_VERSION_MARKER = "name=\"hybrisVersion\""
    private const val VALUE_ATTR = "value=\""
}