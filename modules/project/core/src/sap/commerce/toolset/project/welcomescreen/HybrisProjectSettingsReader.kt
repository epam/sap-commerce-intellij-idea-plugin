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

package sap.commerce.toolset.project.welcomescreen

import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.util.fileExists
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

/**
 * Stateless reader for `.idea/hybrisProjectSettings.xml`.
 *
 * Uses StAX (streaming XML reader) for minimal allocations and early exit
 * once all wanted fields are read. Returns an empty [Settings] on any failure
 * or missing file — callers treat absent fields as "unknown".
 */
object HybrisProjectSettingsReader {

    data class Settings(
        val hybrisVersion: String? = null
    )

    private val factory: XMLInputFactory = XMLInputFactory.newInstance().apply {
        setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
        setProperty(XMLInputFactory.SUPPORT_DTD, false)
    }

    fun read(projectLocation: String): Settings {
        val settingsFile = Path.of(projectLocation)
            .resolve(ProjectConstants.Directory.IDEA)
            .resolve(SETTINGS_FILE_NAME)

        if (!settingsFile.fileExists) return Settings()

        return runCatching {
            Files.newInputStream(settingsFile).use { input ->
                val reader = factory.createXMLStreamReader(input)
                try {
                    parse(reader)
                } finally {
                    reader.close()
                }
            }
        }.getOrElse { Settings() }
    }

    private fun parse(reader: XMLStreamReader): Settings {
        var hybrisVersion: String? = null

        while (reader.hasNext()) {
            if (reader.next() != XMLStreamConstants.START_ELEMENT) continue
            if (reader.localName != "option") continue

            val name = reader.getAttributeValue(null, "name") ?: continue
            val value = reader.getAttributeValue(null, "value") ?: continue

            when (name) {
                "hybrisVersion" -> hybrisVersion = value
            }

            if (hybrisVersion != null) break  // early exit once we have everything
        }

        return Settings(hybrisVersion = hybrisVersion)
    }

    private const val SETTINGS_FILE_NAME = "hybrisProjectSettings.xml"
}