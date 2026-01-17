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

package sap.commerce.toolset.ant

import sap.commerce.toolset.HybrisConstants
import java.util.regex.Pattern
import kotlin.io.path.Path

object AntConstants {

    val PATH_ANT_LIB = Path("resources", "ant", "lib")
    val PATTERN_APACHE_ANT: Pattern = Pattern.compile("apache-ant.*")

    const val ANT_ENCODING = "-Dfile.encoding=UTF-8"
    const val ANT_HYBRIS_CONFIG_DIR = "-J-D${HybrisConstants.PROPERTY_HYBRIS_CONFIG_DIR}="
    const val ANT_XMX = "-Xmx"
    const val ANT_PLATFORM_HOME = "PLATFORM_HOME"
    const val ANT_OPTS = "ANT_OPTS"
    const val ANT_HOME = "ANT_HOME"
    const val ANT_BUILD_XML = "build.xml"
    const val ANT_HEAP_SIZE_MB = 512
    const val ANT_STACK_SIZE_MB = 128

    val DESIRABLE_PLATFORM_TARGETS = listOf(
        "clean",
        "build",
        "all",
        "addonclean",
        "alltests",
        "allwebtests",
        "apidoc",
        "bugprooftests",
        "classpathgen",
        "cleanMavenDependencies",
        "cleanear",
        "clearAdministrationLock",
        "clearOrphanedTypes",
        "codequality",
        "commonwebclean",
        "copyFromTemplate",
        "createConfig",
        "createPlatformImageStructure",
        "createtypesystem",
        "customize",
        "demotests",
        "deploy",
        "deployDist",
        "deployDistWithSources",
        "dist",
        "distWithSources",
        "droptypesystem",
        "ear",
        "executeScript",
        "executesql",
        "extensionsxml",
        "extgen",
        "generateLicenseOverview",
        "gradle",
        "importImpex",
        "initialize",
        "initializetenantdb",
        "integrationtests",
        "localizationtest",
        "localproperties",
        "manualtests",
        "metadata",
        "modulegen",
        "performancetests",
        "production",
        "runcronjob",
        "sanitycheck",
        "sassclean",
        "sasscompile",
        "server",
        "sonarcheck",
        "sourcezip",
        "startAdminServer",
        "startHybrisServer",
        "syncaddons",
        "testMavenDependencies",
        "typecodetest",
        "unittests",
        "updateMavenDependencies",
        "updateSpringXsd",
        "updatesystem",
        "webservice_nature",
        "yunitinit",
        "yunitupdate"
    )
    val DESIRABLE_CUSTOM_TARGETS = listOf(
        "clean",
        "build",
        "deploy",
        "all",
        "gensource",
        "dist"
    )
    val META_TARGETS = listOf(
        listOf("clean", "all"),
        listOf("clean", "customize", "all", "initialize"),
        listOf("clean", "customize", "all", "production")
    )

    object Target {
        const val UPDATE_MAVEN_DEPENDENCIES = "updateMavenDependencies"
    }
}