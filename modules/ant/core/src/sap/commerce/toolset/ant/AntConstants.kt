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

import java.util.regex.Pattern

object AntConstants {

    val PATTERN_APACHE_ANT: Pattern = Pattern.compile("apache-ant.*")

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
}