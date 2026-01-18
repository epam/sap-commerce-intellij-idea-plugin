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
import sap.commerce.toolset.extensioninfo.EiConstants
import kotlin.io.path.Path

object ProjectConstants {

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
        const val ADDON_TEST_SRC = "addontestsrc"

        const val RESOURCES = "resources"
        const val ECLIPSE_BIN = "eclipsebin"
        const val NPM = "npm"
        const val NODE_MODULES = "node_modules"
        const val JS_STOREFRONT = "js-storefront"

        const val BUILD_TOOLS = "build-tools"
        const val LICENSES = "licenses"
        const val LICENCE = "licence"
        const val BOWER_COMPONENTS = "bower_components"
        const val JS_TARGET = "jsTarget"

        const val EXT = "ext"
        const val INSTALLER = "installer"
        const val ACCELERATOR_ADDON = "acceleratoraddon"
        const val WEB_ROOT = "webroot"

        val SRC_DIR_NAMES = arrayOf(SRC, GROOVY_SRC, SCALA_SRC)
        val TEST_SRC_DIR_NAMES = arrayOf(TEST_SRC, GROOVY_TEST_SRC, SCALA_TEST_SRC)
    }

    object File {
        const val LOCAL_PROPERTIES = "local.properties"
        const val PROJECT_PROPERTIES = "project.properties"
        const val PLATFORM_HOME_PROPERTIES = "platformhome.properties"
        const val ENV_PROPERTIES = "env.properties"
        const val ADVANCED_PROPERTIES = "advanced.properties"

        const val EXTENSIONS_XML = "extensions.xml"
        const val BUILD_CALLBACKS_XML = "buildcallbacks.xml"

        const val HYBRIS_LICENCE_JAR = "hybrislicence.jar"
        const val SAP_LICENCES = "installedSaplicenses.properties"
    }

    object Paths {
        val IDEA_MODULES = Path(".idea", "idea-modules")

        val RELATIVE_DOC_SOURCES = Path("..", "doc", "sources")
        val RELATIVE_CONFIG = Path("..", "..", "config")

        val PLATFORM_BOOTSTRAP = Path("platform", "bootstrap")
        val BIN_PLATFORM = Path("bin", "platform")
        val BIN_PLATFORM_BUILD_NUMBER = Path("bin", "platform", "build.number")

        val BIN_CUSTOM = Path("bin", "custom")

        val LIB_DB_DRIVER = Path("lib", "dbdriver")
        val ADVANCED_PROPERTIES = Path("resources", "advanced.properties")

        val BOOTSTRAP_BIN = Path("bootstrap", "bin")
        val BOOTSTRAP_GEN_SRC = Path("bootstrap", "gensrc")

        val TOMCAT_BIN = Path("tomcat", "bin")
        val TOMCAT_LIB = Path("tomcat", "lib")
        val TOMCAT_6_BIN = Path("tomcat-6", "bin")
        val TOMCAT_6_LIB = Path("tomcat-6", "lib")

        val DOC_SOURCES = Path("doc", "sources")
        val ACCELERATOR_ADDON_WEB = Path("acceleratoraddon", "web")

        val WEBROOT_WEB_INF_LIB = Path("webroot", "WEB-INF", "lib")
        val WEBROOT_WEB_INF_CLASSES = Path("webroot", "WEB-INF", "classes")
        val WEBROOT_WEB_INF_WEB_XML = Path("webroot", "WEB-INF", "web.xml")

        val BACKOFFICE_JAR = Path("resources", "backoffice")

        val RESERVED_TYPE_CODES_FILE = Path("resources", "core", "unittest", "reservedTypecodes.txt")

        val HYBRIS_SERVER_SHELL_SCRIPT_NAME = Path("bin", "platform", "hybrisserver.sh")
        val HYBRIS_SERVER_BASH_SCRIPT_NAME = Path("bin", "platform", "hybrisserver.bat")
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
