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

import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.util.Key
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import java.nio.file.Path
import kotlin.io.path.Path

object ProjectConstants {

    @JvmStatic
    val KEY_FINALIZE_PROJECT_IMPORT: Key<ProjectImportContext> = Key.create("hybrisProjectImportFinalize")
    val Y_MODULE_TYPE_ID = JavaModuleType.getModuleType().id

    object Directory {
        const val BOOTSTRAP = "bootstrap"

        const val TMP = "tmp"
        const val TC_SERVER = "tcServer"
        const val TOMCAT = "tomcat"
        const val TOMCAT_6 = "tomcat-6"
        const val ANT = "apache-ant"
        const val DATA = "data"
        const val LOG = "log"
        const val LIB = "lib"
        const val BIN = "bin"
        const val TEMP = "temp"
        const val GRADLE = ".gradle"
        const val SVN = ".svn"
        const val GIT = ".git"
        const val HG = ".hg"
        const val GITHUB = ".github"
        const val IDEA = ".idea"
        const val ANGULAR = ".angular"
        const val SETTINGS = ".settings"
        const val MACO_SX = "__MACOSX"
        const val IDEA_MODULE_FILES = "idea-module-files"

        const val CLASSES = "classes"
        const val TEST_CLASSES = "testclasses"
        const val MODEL_CLASSES = "modelclasses"

        const val SRC = "src"
        const val GEN_SRC = "gensrc"
        const val ADDON_SRC = "addonsrc"
        const val COMMON_WEB_SRC = "commonwebsrc"
        const val GROOVY_SRC = "groovysrc"
        const val KOTLIN_SRC = "kotlinsrc"
        const val SCALA_SRC = "scalasrc"

        const val TEST_SRC = "testsrc"
        const val GROOVY_TEST_SRC = "groovytestsrc"
        const val KOTLIN_TEST_SRC = "kotlintestsrc"
        const val SCALA_TEST_SRC = "scalatestsrc"

        const val RESOURCES = "resources"
        const val ECLIPSE_BIN = "eclipsebin"
        const val NODE_MODULES = "node_modules"
        const val JS_STOREFRONT = "js-storefront"

        const val BUILD_TOOLS = "build-tools"
        const val LICENSES = "licenses"
        const val LICENCE = "licence"
        const val BOWER_COMPONENTS = "bower_components"
        const val JS_TARGET = "jsTarget"

        const val EXT = "ext"
        const val HYBRIS = "hybris"
        const val INSTALLER = "installer"
        const val ACCELERATOR_ADDON = "acceleratoraddon"
        const val WEB_ROOT = "webroot"

        val PATH_PLATFORM_BOOTSTRAP: Path = Path("platform", BOOTSTRAP)
        val PATH_BIN_PLATFORM: Path = Path(BIN, "platform")
        val PATH_BIN_PLATFORM_BUILD_NUMBER: Path = PATH_BIN_PLATFORM.resolve("build.number")
        val PATH_BIN_CUSTOM = Path(BIN, "custom")
        val PATH_LIB_DB_DRIVER: Path = Path("lib", "dbdriver")
        val PATH_IDEA_MODULES: Path = Path(".idea", "idea-modules")

        @JvmField
        val SRC_DIR_NAMES = listOf(SRC, GROOVY_SRC, KOTLIN_SRC, SCALA_SRC)

        @JvmField
        val ALL_SRC_DIR_NAMES = listOf(GEN_SRC, TEST_SRC, SRC, GROOVY_SRC, KOTLIN_SRC, SCALA_SRC)

        @JvmField
        val TEST_SRC_DIR_NAMES = listOf(TEST_SRC, GROOVY_TEST_SRC, KOTLIN_TEST_SRC, SCALA_TEST_SRC)
    }

    object File {
        const val LOCAL_PROPERTIES = "local.properties"
        const val PROJECT_PROPERTIES = "project.properties"
        const val PLATFORM_HOME_PROPERTIES = "platformhome.properties"
        const val ENV_PROPERTIES = "env.properties"
        const val ADVANCED_PROPERTIES = "advanced.properties"
    }

    val PLATFORM_EXTENSION_NAMES = setOf(
        EiConstants.Extension.ADVANCED_SAVED_QUERY,
        EiConstants.Extension.CATALOG,
        EiConstants.Extension.COMMENTS,
        EiConstants.Extension.COMMONS,
        EiConstants.Extension.CORE,
        EiConstants.Extension.DELIVERY_ZONE,
        EiConstants.Extension.EUROPE1,
        EiConstants.Extension.HAC,
        EiConstants.Extension.IMPEX,
        EiConstants.Extension.MAINTENANCE_WEB,
        EiConstants.Extension.MEDIA_WEB,
        EiConstants.Extension.OAUTH2,
        EiConstants.Extension.PAYMENT_STANDARD,
        EiConstants.Extension.PLATFORM_SERVICES,
        EiConstants.Extension.PROCESSING,
        EiConstants.Extension.SCRIPTING,
        EiConstants.Extension.TEST_WEB,
        EiConstants.Extension.VALIDATION,
        EiConstants.Extension.WORKFLOW,
    )

}