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

package sap.commerce.toolset.project

import com.intellij.openapi.util.Key
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import java.nio.file.Path
import kotlin.io.path.Path

object ProjectConstants {

    @JvmStatic
    val KEY_FINALIZE_PROJECT_IMPORT: Key<HybrisProjectDescriptor> = Key.create("hybrisProjectImportFinalize")

    object ExcludeDirectories {
        val BOOTSTRAP: Path = Path("platform", "bootstrap")

        const val TMP = "tmp"
        const val TC_SERVER = "tcServer"
        const val TOMCAT = "tomcat"
        const val TOMCAT_6 = "tomcat-6"
        const val ANT = "apache-ant"
        const val DATA = "data"
        const val LOG = "log"
        const val LIB = "lib"
        const val TEMP = "temp"
        const val GRADLE = ".gradle"
        const val SVN = ".svn"
        const val GIT = ".git"
        const val HG = ".hg"
        const val GITHUB = ".github"
        const val IDEA = ".idea"
        const val MACO_SX = "__MACOSX"
        const val IDEA_MODULE_FILES = "idea-module-files"
        const val CLASSES = "classes"
        const val TEST_CLASSES_DIRECTORY = "testclasses"
        const val SRC = "src"
        const val GROOVY_SRC = "groovysrc"
        const val KOTLIN_SRC = "kotlinsrc"
        const val TEST_SRC = "testsrc"
        const val GROOVY_TEST_SRC = "groovytestsrc"
        const val KOTLIN_TEST_SRC = "kotlintestsrc"
        const val RESOURCES = "resources"
        const val ECLIPSE_BIN = "eclipsebin"
        const val NODE_MODULES = "node_modules"
        const val JS_STOREFRONT = "js-storefront"
    }

}