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

package sap.commerce.toolset.localextensions

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import sap.commerce.toolset.localextensions.context.FoundExtension
import sap.commerce.toolset.localextensions.context.LocalExtensionsContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.pathString
import kotlin.io.path.writeText

class LocalExtensionsProcessorTest : BasePlatformTestCase() {

    private lateinit var tempDirectory: Path

    override fun setUp() {
        super.setUp()
        tempDirectory = Files.createTempDirectory("localextensions-processor-test")
    }

    override fun tearDown() {
        try {
            tempDirectory.toFile().deleteRecursively()
        } finally {
            super.tearDown()
        }
    }

    fun testCreatesContextForExplicitDirOnlyExtensions() = runBlocking {
        val configDirectory = tempDirectory.resolve("config").createDirectories()
        val platformDirectory = tempDirectory.resolve("hybris")
        platformDirectory.resolve("bin/platform").createDirectories()
            .resolve("env.properties")
            .writeText("")
        val extensionDirectory = tempDirectory.resolve("custom/sampleextension").createDirectories()
        extensionDirectory.resolve("extensioninfo.xml").writeText(
            """
                <extensioninfo>
                    <extension name="sampleextension"/>
                </extensioninfo>
            """.trimIndent()
        )
        configDirectory.resolve("localextensions.xml").writeText(
            """
                <hybrisconfig>
                    <extensions>
                        <extension dir="${extensionDirectory.pathString}"/>
                    </extensions>
                </hybrisconfig>
            """.trimIndent()
        )

        val context = kotlin.test.assertNotNull(
            LocalExtensionsProcessor().getContext(
                configDirectory = configDirectory,
                platformDirectory = platformDirectory,
                foundExtensions = listOf(FoundExtension("sampleextension", extensionDirectory)),
            )
        )

        kotlin.test.assertTrue(context.scanTypes.isEmpty())
        kotlin.test.assertEquals(extensionDirectory, context.extensions["sampleextension"]?.path)
    }

    fun testSelectsExplicitlyDeclaredExtensionWithoutScanPaths() {
        val declaredDirectory = tempDirectory.resolve("declared/sampleextension")
        val duplicateDirectory = tempDirectory.resolve("duplicate/sampleextension")
        val context = LocalExtensionsContext(
            extensions = mapOf(
                "sampleextension" to LocalExtensionsContext.Extension("sampleextension", declaredDirectory),
            )
        )

        val extension = LocalExtensionsProcessor().getSuitableExtension(
            foundExtensions = listOf(
                FoundExtension("sampleextension", duplicateDirectory),
                FoundExtension("sampleextension", declaredDirectory),
            ),
            context = context,
        )

        kotlin.test.assertEquals(declaredDirectory, extension?.moduleRootPath)
    }

    fun testRetainsScanPathPriorityForDuplicateExtensions() {
        val preferredDirectory = tempDirectory.resolve("preferred/sampleextension")
        val secondaryDirectory = tempDirectory.resolve("secondary/sampleextension")
        val context = LocalExtensionsContext(
            scanTypes = linkedMapOf(
                "preferred" to ScanType("preferred", false, 1, tempDirectory.resolve("preferred")),
                "secondary" to ScanType("secondary", false, 1, tempDirectory.resolve("secondary")),
            ),
            extensions = mapOf(
                "sampleextension" to LocalExtensionsContext.Extension("sampleextension", secondaryDirectory),
            ),
        )

        val extension = LocalExtensionsProcessor().getSuitableExtension(
            foundExtensions = listOf(
                FoundExtension("sampleextension", secondaryDirectory),
                FoundExtension("sampleextension", preferredDirectory),
            ),
            context = context,
        )

        kotlin.test.assertEquals(preferredDirectory, extension?.moduleRootPath)
    }
}
